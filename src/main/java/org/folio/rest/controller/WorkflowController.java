package org.folio.rest.controller;

import org.folio.rest.annotation.TokenHeader;
import org.folio.rest.exception.UnableToActivateWorkflowException;
import org.folio.rest.exception.UnableToDeactivateWorkflowException;
import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.model.Workflow;
import org.folio.rest.service.CamundaApiService;
import org.folio.rest.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

  @Autowired
  private CamundaApiService camundaApiService;

  @PostMapping("/activate")
  public Workflow activateWorkflow(
    @TenantHeader String tenant,
    @TokenHeader String token,
    @RequestBody Workflow workflow
  ) throws WorkflowAlreadyActiveException, UnableToActivateWorkflowException {

    return camundaApiService.deployWorkflow(workflow, tenant, token);
  }

  @PostMapping("/deactivate")
  public Workflow deactivateWorkflow(
    @TenantHeader String tenant,
    @TokenHeader String token,
      @RequestBody Workflow workflow
  ) throws UnableToDeactivateWorkflowException {

    return camundaApiService.undeployWorkflow(workflow, tenant, token);
  }
}
