package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.camunda.exception.WorkflowHasInvalidNode;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CamundaApiServiceTest {

  private static final String TENANT = "testTenant";

  @Mock
  private BpmnModelFactory bpmnModelFactory;

  @InjectMocks
  private CamundaApiService camundaApiService;

  @Mock
  private BpmnModelInstance modelInstance;

  @Mock
  private ProcessEngine processEngine;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private DeploymentBuilder deploymentBuilder;

  @Mock
  private Deployment deployment;

  private Workflow workflow;

  @BeforeEach
  void setUp() throws ScriptTaskDeserializeCodeFailure, WorkflowHasInvalidNode {
    workflow = new Workflow();
    workflow.setActive(false);
    workflow.setName("Test Workflow");

    lenient().when(bpmnModelFactory.fromWorkflow(workflow)).thenReturn(modelInstance);
    lenient().when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    lenient().when(repositoryService.createDeployment()).thenReturn(deploymentBuilder);
    lenient().when(deploymentBuilder.name(anyString())).thenReturn(deploymentBuilder);
    lenient().when(deploymentBuilder.addModelInstance(anyString(), any(BpmnModelInstance.class)))
      .thenReturn(deploymentBuilder);
    lenient().when(deploymentBuilder.source(anyString())).thenReturn(deploymentBuilder);
    lenient().when(deploymentBuilder.tenantId(anyString())).thenReturn(deploymentBuilder);
    lenient().when(deploymentBuilder.deploy()).thenReturn(deployment);
    lenient().when(deployment.getId()).thenReturn("deploymentId");
  }

  @Test
  void testDeployWorkflow() throws ScriptTaskDeserializeCodeFailure, WorkflowAlreadyActiveException, WorkflowHasInvalidNode {
    try (
        MockedStatic<ProcessEngines> utilityProcessEngines = Mockito.mockStatic(ProcessEngines.class);
        MockedStatic<Bpmn> utilityBpmn = Mockito.mockStatic(Bpmn.class);
      ) {
      utilityProcessEngines.when(ProcessEngines::getDefaultProcessEngine).thenReturn(processEngine);
      utilityBpmn.when(() -> Bpmn.validateModel(modelInstance)).thenAnswer(answer -> null);

      Workflow result = camundaApiService.deployWorkflow(workflow, TENANT);

      assertNotNull(result);
      assertEquals(true, result.getActive());
      assertEquals("deploymentId", result.getDeploymentId());

      verify(bpmnModelFactory).fromWorkflow(workflow);
      verify(repositoryService).createDeployment();
    }
  }

  @Test
  void testDeployWorkflowException() {
    try (
        MockedStatic<ProcessEngines> utilityProcessEngines = Mockito.mockStatic(ProcessEngines.class);
        MockedStatic<Bpmn> utilityBpmn = Mockito.mockStatic(Bpmn.class);
    ) {
      utilityProcessEngines.when(ProcessEngines::getDefaultProcessEngine).thenReturn(processEngine);
      utilityBpmn.when(() -> Bpmn.validateModel(modelInstance)).thenAnswer(answer -> null);

      when(deploymentBuilder.deploy()).thenThrow(new NotValidException());

      assertThrows(NotValidException.class, () -> camundaApiService.deployWorkflow(workflow, TENANT));
    }
  }

  @Test
  void testDeployWorkflowAlreadyActive() throws WorkflowAlreadyActiveException, ScriptTaskDeserializeCodeFailure, WorkflowHasInvalidNode {
    testDeployWorkflow();

    assertThrows(WorkflowAlreadyActiveException.class, () -> camundaApiService.deployWorkflow(workflow, TENANT));
  }

  @Test
  void testUndeployWorkflow() throws ScriptTaskDeserializeCodeFailure, WorkflowAlreadyActiveException, WorkflowHasInvalidNode {
    testDeployWorkflow();

    try (MockedStatic<ProcessEngines> utilityProcessEngines = Mockito.mockStatic(ProcessEngines.class)) {
      utilityProcessEngines.when(ProcessEngines::getDefaultProcessEngine).thenReturn(processEngine);

      Workflow result = camundaApiService.undeployWorkflow(workflow);

      assertNotNull(result);
      assertEquals(false, result.getActive());
      assertNull(result.getDeploymentId());

      verify(repositoryService).deleteDeployment("deploymentId", true);
    }
  }

  @Test
  void testUndeployWorkflowNotActive() {
    try (MockedStatic<ProcessEngines> utilityProcessEngines = Mockito.mockStatic(ProcessEngines.class)) {
      utilityProcessEngines.when(ProcessEngines::getDefaultProcessEngine).thenReturn(processEngine);

      Workflow result = camundaApiService.undeployWorkflow(workflow);

      assertNotNull(result);
      assertEquals(false, result.getActive());
      assertNull(result.getDeploymentId());

      verify(repositoryService, never()).deleteDeployment(anyString(), anyBoolean());
    }
  }
}
