package org.folio.rest.delegate;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.Request;
import org.folio.rest.workflow.model.StreamRequestToDirectoryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Scope("prototype")
public class StreamRequestToDirectoryDelegate extends AbstractWorkflowInputDelegate {

  @Autowired
  private WebClient webClient;

  private Expression request;

  private Expression path;

  private Expression workflow;

  private Expression batchSize;

  private Expression completeMessage;

  private Expression writeSignalMessage;

  private Expression emitWriteSignal;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    Request request = objectMapper.readValue(this.request.getValue(execution).toString(), Request.class);

    String url = request.getUrl();
    HttpMethod method = request.getMethod();
    String accept = request.getAccept();
    String contentType = request.getContentType();

    Map<String, Object> inputs = getInputs(execution);

    StringSubstitutor sub = new StringSubstitutor(inputs);

    String body = sub.replace(request.getBodyTemplate());

    String tenant = execution.getTenantId();

    logger.info("url: {}", url);
    logger.debug("method: {}", method);

    logger.debug("accept: {}", accept);
    logger.debug("content-type: {}", contentType);
    logger.debug("tenant: {}", tenant);

    logger.debug("body: {}", body);

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    File workflowDirectory = new File(String.join(File.separator, path, tenant, workflow));

    String workflowDataPath = workflowDirectory.getAbsolutePath();

    if (!workflowDirectory.exists()) {
      if (workflowDirectory.mkdirs()) {
        logger.info("Created directory {}", workflowDataPath);
      } else {
        throw new RuntimeException(String.format("Failed to create directory %s", workflowDataPath));
      }
    }

    int batchSize = Integer.parseInt(this.batchSize.getValue(execution).toString());

    boolean emitWriteSignal = Boolean.parseBoolean(this.emitWriteSignal.getValue(execution).toString());

    Optional<Object> writeSignalMessage = Optional.ofNullable(this.writeSignalMessage.getValue(execution));

    AtomicInteger count = new AtomicInteger(1);

    // @formatter:off
    webClient
      .method(method)
      .uri(url)
      .bodyValue(body.getBytes())
      .header("Accept", accept)
      .header("Content-Type", contentType)
      .header("X-Okapi-Tenant", tenant)
      .retrieve()
      .bodyToFlux(Map.class)
      .buffer(batchSize)
      .doOnComplete(() -> {
        long endTime = System.nanoTime();
        logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
        String completeMessage = this.completeMessage.getValue(execution).toString();
        logger.info("Emitting message {}", completeMessage);
        runtimeService.createMessageCorrelation(completeMessage)
          .tenantId(tenant)
          .correlate();
      })
      .subscribe(batch -> {
        String filename = StringUtils.leftPad(String.valueOf(count.getAndIncrement()), 16, "0");
        String filePath = String.join(File.separator, workflowDataPath, filename);
        logger.info("Writing file {}", filePath);
        try {
          objectMapper.writeValue(new File(filePath), batch);
          if (emitWriteSignal) {            
            if(writeSignalMessage.isPresent()) {
              inputs.put("file", filePath);
              runtimeService.createSignalEvent(writeSignalMessage.get().toString()).tenantId(tenant).setVariables(inputs).send();
            } else {
              logger.warn("Cannot emit write signal without message!");
            }
          }
        } catch (IOException e) {
          logger.error("Failed writing file {} {}", filePath, e.getMessage());
        }          
    });
    // @formatter:on
  }

  public void setRequest(Expression request) {
    this.request = request;
  }

  public void setPath(Expression path) {
    this.path = path;
  }

  public void setWorkflow(Expression workflow) {
    this.workflow = workflow;
  }

  public void setBatchSize(Expression batchSize) {
    this.batchSize = batchSize;
  }

  public void setCompleteMessage(Expression completeMessage) {
    this.completeMessage = completeMessage;
  }

  public void setWriteSignalMessage(Expression writeSignalMessage) {
    this.writeSignalMessage = writeSignalMessage;
  }

  public void setEmitWriteSignal(Expression emitWriteSignal) {
    this.emitWriteSignal = emitWriteSignal;
  }

  @Override
  public Class<?> fromTask() {
    return StreamRequestToDirectoryTask.class;
  }

}