package org.folio.rest.delegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.script.ScriptException;

import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.delegate.iterable.BufferingStreamIterable;
import org.folio.rest.delegate.iterable.EnhancingStreamIterable;
import org.folio.rest.delegate.iterable.OrderingMergeStreamIterable;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.Comparison;
import org.folio.rest.workflow.model.Extractor;
import org.folio.rest.workflow.model.Mapping;
import org.folio.rest.workflow.model.Processor;
import org.folio.rest.workflow.model.Request;
import org.folio.rest.workflow.model.StreamingExtractTransformLoadTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Service
@Scope("prototype")
public class StreamExtractTransformLoadDelegate extends AbstractWorkflowIODelegate {

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private WebClient webClient;

  @Autowired
  private ScriptEngineService scriptEngineService;

  // TODO: make interface where all three can be in any order, update code below

  private Expression extractors;

  private Expression processors;

  private Expression requests;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    final List<Extractor> extractors = objectMapper.readValue(this.extractors.getValue(execution).toString(),
        new TypeReference<List<Extractor>>() {
        });

    final List<Processor> processors = objectMapper.readValue(this.processors.getValue(execution).toString(),
        new TypeReference<List<Processor>>() {
        });

    final List<Request> requests = objectMapper.readValue(this.requests.getValue(execution).toString(),
        new TypeReference<List<Request>>() {
        });

    runProcessors(processors, runExtractors(execution, extractors))
        .forEach(node -> runRequests(execution, requests, node));

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setExtractors(Expression extractors) {
    this.extractors = extractors;
  }

  public void setProcessors(Expression processors) {
    this.processors = processors;
  }

  public void setRequests(Expression requests) {
    this.requests = requests;
  }

  @Override
  public Class<?> fromTask() {
    return StreamingExtractTransformLoadTask.class;
  }

  private Stream<JsonNode> runExtractors(DelegateExecution execution, List<Extractor> extractors)
      throws JsonMappingException, JsonProcessingException {
    Stream<JsonNode> stream = Stream.empty();
    for (Extractor extractor : extractors) {
      Stream<JsonNode> s = runExtractor(execution, extractor);
      // @formatter:off
      switch (extractor.getMergeStrategy()) {
      case CONCAT: {
        stream = Stream.concat(stream, s);
      }  break;
      case ENHANCE: {
        List<Comparison> comparisons = extractor.getComparisons();
        List<Mapping> mappings = extractor.getMappings();
        stream = EnhancingStreamIterable.of(stream, s, comparisons, mappings).toStream();
      }  break;
      case MERGE: {
        List<Comparison> comparisons = extractor.getComparisons();
        stream = OrderingMergeStreamIterable.of(stream, s, comparisons).toStream();
      }  break;
      default:
        break;
      }
      // @formatter:on
    }
    return stream;
  }

  private Stream<JsonNode> runExtractor(DelegateExecution execution, Extractor extractor)
      throws JsonMappingException, JsonProcessingException {
    Request request = extractor.getRequest();
    String url = request.getUrl();
    HttpMethod method = request.getMethod();
    String accept = request.getAccept();
    String contentType = request.getContentType();

    Map<String, Object> inputs = getInputs(execution);

    StringSubstitutor sub = new StringSubstitutor(inputs);

    String body = sub.replace(request.getBodyTemplate());

    String tenant = execution.getTenantId();

    // @formatter:off
    return webClient
      .method(method)
      .uri(url)
      .bodyValue(body.getBytes())
      .header("Accept", accept)
      .header("Content-Type", contentType)
      .header("X-Okapi-Tenant", tenant)
      .retrieve()
      .bodyToFlux(JsonNode.class)
      .toStream();
    // @formatter:on
  }

  private Stream<JsonNode> runProcessors(List<Processor> processors, Stream<JsonNode> stream) {
    for (Processor processor : processors) {
      stream = runProcessor(processor, stream);
    }
    return stream;
  }

  private Stream<JsonNode> runProcessor(Processor processor, Stream<JsonNode> stream) {

    final String scriptName = processor.getFunctionName();
    final String scriptTypeExtension = processor.getScriptType().getExtension();

    stream = stream.map(input -> {
      Optional<JsonNode> node = Optional.empty();
      try {
        String output = (String) scriptEngineService.runScript(scriptTypeExtension, scriptName, input);

        try {
          node = Optional.ofNullable(objectMapper.readTree(output));
        } catch (JsonMappingException e) {
          e.printStackTrace();
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (ScriptException e) {
        e.printStackTrace();
      }
      return node;
    }).filter(node -> node.isPresent()).map(node -> node.get());

    int buffer = processor.getBuffer();
    if (buffer > 0) {
      int delay = processor.getDelay();
      return BufferingStreamIterable.of(stream, buffer, delay).toStream().map(batch -> objectMapper.valueToTree(batch));
    }
    return stream;
  }

  private void runRequests(DelegateExecution execution, List<Request> requests, JsonNode node) {
    // @formatter:off
    Map<String, Object> inputs = objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
    runRequests(execution, requests, inputs);
    // @formatter:on
  }

  private void runRequests(DelegateExecution execution, List<Request> requests, Map<String, Object> inputs) {
    for (Request request : requests) {
      runRequest(execution, request, inputs);
    }
  }

  private void runRequest(DelegateExecution execution, Request request, Map<String, Object> inputs) {
    String url = request.getUrl();
    HttpMethod method = request.getMethod();
    String accept = request.getAccept();
    String contentType = request.getContentType();

    StringSubstitutor sub = new StringSubstitutor(inputs);

    String payload = sub.replace(request.getBodyTemplate());

    byte[] body = payload.getBytes();

    String tenant = execution.getTenantId();

    Optional<Object> token = Optional.ofNullable(execution.getVariable("token"));

    logger.info("url: {}", url);
    logger.debug("method: {}", method);

    logger.debug("accept: {}", accept);
    logger.debug("content-type: {}", contentType);
    logger.debug("tenant: {}", tenant);

    logger.debug("body: {}", body);

    String contentLength = String.valueOf(body.length);
    // @formatter:off
    webClient
      .post()
      .uri(url)
      .bodyValue(body)
      .header("X-Okapi-Url", OKAPI_LOCATION)
      .header("X-Okapi-Tenant", tenant)
      .header("X-Okapi-Token", token.isPresent() ? token.get().toString() : null)
      .header(HttpHeaders.CONTENT_TYPE, contentType)
      .header(HttpHeaders.CONTENT_LENGTH, contentLength)
      .header(HttpHeaders.ACCEPT, accept)
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError, response -> {
        logger.error("status: {}", response.statusCode());
        return Mono.empty();
      })
      .onStatus(HttpStatus::is5xxServerError, response -> {
        logger.error("status: {}", response.statusCode());
        return Mono.empty();
      })
      .bodyToMono(String.class)
      .subscribe();
    // @formatter:on
  }

}