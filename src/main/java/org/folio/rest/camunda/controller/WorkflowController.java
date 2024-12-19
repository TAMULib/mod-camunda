package org.folio.rest.camunda.controller;

import lombok.extern.slf4j.Slf4j;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.camunda.service.CamundaApiService;
import org.folio.rest.workflow.model.Workflow;
import org.folio.spring.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/workflow-engine/workflows")
public class WorkflowController {

  private CamundaApiService camundaApiService;

  @Autowired
  public WorkflowController(CamundaApiService camundaApiService) {
    this.camundaApiService = camundaApiService;
  }

  @PostMapping(value = "/activate", produces = { MediaType.APPLICATION_JSON_VALUE })
  public Workflow activateWorkflow(@RequestBody Workflow workflow, @TenantHeader String tenant)
      throws WorkflowAlreadyActiveException, ScriptTaskDeserializeCodeFailure {
    log.debug("Activating Workflow: {}", workflow == null ? null : workflow.getId());
    return camundaApiService.deployWorkflow(workflow, tenant);
  }

  @PostMapping(value = "/deactivate", produces = { MediaType.APPLICATION_JSON_VALUE })
  public Workflow deactivateWorkflow(@RequestBody Workflow workflow) {
    log.debug("Deactivating Workflow: {}", workflow == null ? null : workflow.getId());
    return camundaApiService.undeployWorkflow(workflow);
  }
}
