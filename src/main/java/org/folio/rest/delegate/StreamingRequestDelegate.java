package org.folio.rest.delegate;

import java.time.Instant;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.folio.rest.workflow.components.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
@Scope("prototype")
public class StreamingRequestDelegate extends AbstractReportableDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StreamService streamService;

  @Autowired
  private WebClient webClient;

  private Expression requests;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    super.execute(execution);
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String tenant = execution.getTenantId() != null ? execution.getTenantId() : DEFAULT_TENANT;

    String serializedRequests = requests.getValue(execution).toString();

    List<Request> requests = objectMapper.readValue(serializedRequests, new TypeReference<List<Request>>() {});

    String token = (String) execution.getVariable("token");
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    updateReport(primaryStreamId, String.format("%s STARTED AT %s",delegateName, Instant.now()));

    requests.forEach(request -> {

      String storageDestination = request.getStorageDestination();
      String sourceProperty = request.getSourceProperty();
      String contentType = request.getContentType();
      String accept = request.getAccept();

      streamService.map(primaryStreamId, d -> {
        try {
          JsonNode node = objectMapper.readTree(d);
          
          byte[] body = objectMapper.writeValueAsBytes(node.get(sourceProperty));

          String contentLength = String.valueOf(body.length);
          webClient
            .post()
            .uri(storageDestination)
            .bodyValue(body)
            .header("X-Okapi-Url", OKAPI_LOCATION)
            .header("X-Okapi-Tenant", tenant)
            .header("X-Okapi-Token", token)
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .header(HttpHeaders.CONTENT_LENGTH, contentLength)
            .header(HttpHeaders.ACCEPT, accept)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response -> {
              log.error(String.format("Response status: %s", response.statusCode()));
              return Mono.empty();
            })
            .onStatus(HttpStatus::is5xxServerError, response -> {
              log.error(String.format("Response status: %s", response.statusCode()));
              return Mono.empty();
            })
            .bodyToMono(String.class)
            .subscribe();
          updateReport(primaryStreamId, String.format("Sent POST to Storage Destination: %s", d));
        } catch (JsonProcessingException e) {
          log.error(String.format("Error: %s", e.getMessage()));
        }
        return d;
      });
    });
  }

  public Expression getRequests() {
    return requests;
  }

  public void setRequests(Expression requests) {
    this.requests = requests;
  }

}