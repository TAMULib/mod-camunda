package org.folio.rest.camunda.service;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.folio.rest.camunda.delegate.AbstractWorkflowDelegate;
import org.folio.rest.camunda.delegate.InputDelegate;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.camunda.listener.LogListener;
import org.folio.rest.workflow.enums.StartEventType;
import org.folio.rest.workflow.model.EndEvent;
import org.folio.rest.workflow.model.InputTask;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.Setup;
import org.folio.rest.workflow.model.StartEvent;
import org.folio.rest.workflow.model.Workflow;
import org.folio.spring.test.helper.MapperHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.builder.EndEventBuilder;
import org.operaton.bpm.model.bpmn.builder.ProcessBuilder;
import org.operaton.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.operaton.bpm.model.bpmn.builder.StartEventBuilder;
import org.operaton.bpm.model.bpmn.builder.UserTaskBuilder;
import org.operaton.bpm.model.bpmn.instance.ExtensionElements;
import org.operaton.bpm.model.bpmn.instance.Process;
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonField;
import org.operaton.bpm.model.xml.instance.ModelElementInstance;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class BpmnModelFactoryTest {

  private static final String UUID_NODE_1 = "ea99f40c-832d-4f2c-b81b-cf4c1638daa5";

  private static final String UUID_NODE_2 = "ea99f40c-832d-4f2c-b81b-cf4c1638daa5";

  private static final String UUID_NODE_3 = "789ee60d-e24f-4c40-adcb-984d68fab24d";

  @Mock
  private BpmnModelInstance bpmnModelInstance;

  @Mock
  private OperatonField camundaField;

  @Mock
  private ExtensionElements extensionElements;

  @Mock
  private ModelElementInstance modelElementInstance;

  @Mock
  private Process process;

  @Mock
  private ProcessBuilder processBuilderMocked;

  @Mock
  private StartEventBuilder startEventBuilder;

  @Mock
  private ServiceTaskBuilder serviceTaskBuilder;

  @Mock
  private UserTaskBuilder userTaskBuilder;

  @Mock
  private EndEventBuilder endEventBuilder;

  @Mock
  private RuntimeService runtimeService;

  private JsonMapper mapper;

  @Spy
  private List<AbstractWorkflowDelegate> workflowDelegates;

  private BpmnModelFactory bpmnModelFactory;

  private ProcessBuilder processBuilder;

  private EndEvent endEvent;

  private Node inputNode;

  private Node simpleNode;

  private StartEvent startEvent;

  private List<Node> nodes;

  private Setup setup;

  private Workflow workflow;

  @BeforeEach
  void beforeEach() {
    mapper = spy(MapperHelper.build());

    bpmnModelFactory = new BpmnModelFactory(mapper, workflowDelegates);

    startEvent = new StartEvent();
    startEvent.setId(UUID_NODE_1);
    startEvent.setName(VALUE);
    startEvent.setType(StartEventType.NONE);

    inputNode = new InputTask();
    inputNode.setId(UUID_NODE_2);
    inputNode.setName(VALUE);

    endEvent = new EndEvent();
    endEvent.setId(UUID_NODE_3);
    endEvent.setName(VALUE);

    simpleNode = new MyNode();
    simpleNode.setId(UUID_NODE_1);
    simpleNode.setName(VALUE);

    nodes = new ArrayList<>();

    setup = new Setup();

    workflow = new Workflow();
    workflow.setId(UUID);
    workflow.setSetup(setup);

    processBuilder = new ProcessBuilder(bpmnModelInstance, process);
  }

  @Test
  void testFromWorkflowExceptionDuringSetupUsingWarnings() throws ScriptTaskDeserializeCodeFailure, JacksonException {
    try (MockedStatic<Bpmn> utility = mockStatic(Bpmn.class)) {
      commonUnmockedProcessBuilder(utility);
      commonMockingsBasic();

      // The internal code will handle the exception and a non-NULL value should still be returned.
      // This happens in setup() where a logger.warn prints "Failed to serialize processor scripts".
      // There should be two warning log messages printed "Failed to serialize initial context" and "Failed to serialize processor scripts". 
      when(mapper.writeValueAsString(any())).thenThrow(new MyException(VALUE));

      assertNotNull(bpmnModelFactory.fromWorkflow(workflow));
    }
  }

  @Test
  void testFromWorkflowNoNodesWorks() throws ScriptTaskDeserializeCodeFailure {
    try (MockedStatic<Bpmn> utility = mockStatic(Bpmn.class)) {
      commonUnmockedProcessBuilder(utility);
      commonMockingsBasic();

      assertNotNull(bpmnModelFactory.fromWorkflow(workflow));
    }
  }

  @Test
  void testFromWorkflowMustStartWithStartEvent() {
    nodes.add(simpleNode);
    workflow.setNodes(nodes);

    try (MockedStatic<Bpmn> utility = mockStatic(Bpmn.class)) {
      commonMockedProcessBuilder(utility);

      when(processBuilderMocked.startEvent(anyString())).thenReturn(startEventBuilder);

      RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
        bpmnModelFactory.fromWorkflow(workflow);
      });

      assertNotNull(exception);
      assertTrue(exception.getMessage().contains("Workflow must start with a start event"));
    }
  }

  @Test
  void testFromWorkflowForFullProcess() throws ScriptTaskDeserializeCodeFailure {
    nodes.add(startEvent);
    nodes.add(inputNode);
    nodes.add(endEvent);
    workflow.setNodes(nodes);

    try (MockedStatic<Bpmn> utility = mockStatic(Bpmn.class)) {
      commonMockedProcessBuilder(utility);
      commonMockingsBasic();

      when(processBuilderMocked.startEvent(anyString())).thenReturn(startEventBuilder);

      when(workflowDelegates.stream()).thenAnswer(invocation -> {
        List<AbstractWorkflowDelegate> mockDelegates = new ArrayList<>();
        mockDelegates.add(new InputDelegate(mapper, runtimeService));
        return mockDelegates.stream();
      });

      assertNotNull(bpmnModelFactory.fromWorkflow(workflow));
    }
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   *
   * This function uses the instantiated and unmocked ProcessBuilder.
   *
   * @param utility The mocked static bmp class utility instance.
   */
  private void commonUnmockedProcessBuilder(MockedStatic<Bpmn> utility) {
    utility.when(Bpmn::createExecutableProcess).thenReturn(processBuilder);
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   *
   * This function uses the mocked ProcessBuilder.
   *
   * @param utility The mocked static bmp class utility instance.
   */
  private void commonMockedProcessBuilder(MockedStatic<Bpmn> utility) {
    utility.when(Bpmn::createExecutableProcess).thenReturn(processBuilderMocked);

    lenient().when(startEventBuilder.id(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.name(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.operatonExecutionListenerClass(anyString(), eq(LogListener.class))).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.message(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.timerWithCycle(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.signal(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.interrupting(anyBoolean())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.serviceTask(anyString())).thenReturn(serviceTaskBuilder);
    lenient().when(startEventBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(serviceTaskBuilder.name(anyString())).thenReturn(serviceTaskBuilder);
    lenient().when(serviceTaskBuilder.operatonDelegateExpression(anyString())).thenReturn(serviceTaskBuilder);
    lenient().when(serviceTaskBuilder.userTask(anyString())).thenReturn(userTaskBuilder);
    lenient().when(serviceTaskBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(userTaskBuilder.endEvent(anyString())).thenReturn(endEventBuilder);
    lenient().when(userTaskBuilder.name(anyString())).thenReturn(userTaskBuilder);
    lenient().when(userTaskBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(endEventBuilder.name(anyString())).thenReturn(endEventBuilder);
    lenient().when(endEventBuilder.operatonExecutionListenerClass(anyString(), eq(LogListener.class))).thenReturn(endEventBuilder);
    lenient().when(endEventBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(processBuilderMocked.startEvent(anyString())).thenReturn(startEventBuilder);

    lenient().when(processBuilderMocked.getElement()).thenReturn(process);
    lenient().when(processBuilderMocked.name(any())).thenReturn(processBuilderMocked);
    lenient().when(processBuilderMocked.operatonHistoryTimeToLive(anyInt())).thenReturn(processBuilderMocked);
    lenient().when(processBuilderMocked.operatonVersionTag(any())).thenReturn(processBuilderMocked);
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   */
  private void commonMockingsBasic() {
    when(bpmnModelInstance.newInstance(ExtensionElements.class)).thenReturn(extensionElements);
    when(bpmnModelInstance.newInstance(OperatonField.class)).thenReturn(camundaField);
    doNothing().when(extensionElements).addChildElement(any());
    when(bpmnModelInstance.getModelElementById(anyString())).thenReturn(modelElementInstance);
  }

  /**
   * Provide an exception that exposes the string initializer for easy usage.
   */
  private class MyException extends JacksonException {

    private static final long serialVersionUID = -6261961424503639802L;

    public MyException(String msg) {
      super(msg);
    }
  }

  /**
   * Provide a non-abstract Node for easy instantiation.
   */
  private class MyNode extends Node {
  }

}
