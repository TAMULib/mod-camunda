package org.folio.rest.delegate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class FileExtractorDelegate extends AbstractReportableDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Autowired
  private StreamService streamService;

  private Expression path;

  private Expression workflow;

  private Expression delay;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    super.execute(execution);
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String tenant = execution.getTenantId() != null ? execution.getTenantId() : DEFAULT_TENANT;

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    File workflowDirectory = new File(String.join(File.separator, path, tenant, workflow));
    if (workflowDirectory.exists()) {
      long delay = Long.parseLong(this.delay.getValue(execution).toString());

      String primaryStreamId = (String) execution.getVariable("primaryStreamId");

      updateReport(primaryStreamId, String.format("%s STARTED AT %s", delegateName, Instant.now()));

      Stream<String> requestStream = Arrays.asList(workflowDirectory.listFiles()).stream()
          .filter(file -> file.isFile() && !file.getName().startsWith(".")).map(file -> {
            Optional<String> request = Optional.empty();
            try {
              request = Optional.of(IOUtils.toString(file.toURI(), StandardCharsets.UTF_8));
              String renamedPath = file.getAbsolutePath().replace(file.getName(), String.format(".%s", file.getName()));
              file.renameTo(new File(renamedPath));
              Thread.sleep(delay);
            } catch (InterruptedException | IOException e) {
              String errmsg = String.format("Failed to write file %s: %s", file.getAbsolutePath(), e.getMessage());
              log.error(errmsg);
              updateReport(primaryStreamId, errmsg);
            }
            return request;
          }).filter(request -> request.isPresent()).map(request -> request.get());

      streamService.concatenateStream(primaryStreamId, requestStream);
    } else {
      log.error("%s directory does not exists!", String.join(File.separator, path, tenant, workflow));
    }

    log.info("FILE EXTRACTOR DELEGATE FINISHED");
  }

  public Expression getPath() {
    return path;
  }

  public void setPath(Expression path) {
    this.path = path;
  }

  public Expression getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Expression workflow) {
    this.workflow = workflow;
  }

  public Expression getDelay() {
    return delay;
  }

  public void setDelay(Expression delay) {
    this.delay = delay;
  }

}