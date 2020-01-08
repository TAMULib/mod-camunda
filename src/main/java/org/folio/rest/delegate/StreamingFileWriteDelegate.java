package org.folio.rest.delegate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class StreamingFileWriteDelegate extends AbstractReportableDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Autowired
  private StreamService streamService;

  private Expression path;

  private Expression workflow;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    super.execute(execution);
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String tenant = execution.getTenantId() != null ? execution.getTenantId() : DEFAULT_TENANT;

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    File rootDirectory = new File(String.join(File.separator, path));
    if (!rootDirectory.exists()) {
      rootDirectory.mkdir();
    }

    File tenantDirectory = new File(String.join(File.separator, path, tenant));
    if (!tenantDirectory.exists()) {
      tenantDirectory.mkdir();
    }

    File workflowDirectory = new File(String.join(File.separator, path, tenant, workflow));
    if (!workflowDirectory.exists()) {
      workflowDirectory.mkdir();
    }

    String workflowDataPath = workflowDirectory.getAbsolutePath();

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    updateReport(primaryStreamId, String.format("%s STARTED AT %s", delegateName, Instant.now()));

    AtomicInteger index = new AtomicInteger(1);

    streamService.map(primaryStreamId, d -> {
      String name = String.format("%09d", index.getAndIncrement());
      byte[] content = d.getBytes(StandardCharsets.UTF_8);
      String filePath = String.join(File.separator, workflowDataPath, name);
      try {
        Files.write(Paths.get(filePath), content);
        updateReport(primaryStreamId, String.format("Created JSON request file: %s", filePath));
      } catch (IOException e) {
        String errmsg = String.format("Failed to write file %s: %s", filePath, e.getMessage());
        log.error(errmsg);
        updateReport(primaryStreamId, errmsg);
      }
      return d;
    });

    log.info("STREAMING FILE WRITER DELEGATE FINISHED");
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

}