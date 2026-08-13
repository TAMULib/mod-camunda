package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractWorkflowIODelegateTest {

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression inputVariables;

  @Mock
  private Expression outputVariable;

  @InjectMocks
  private ObjectMapper objectMapper;

  @Spy
  private Impl abstractWorkflowIODelegate;

  @Test
  void testGetOutputVariableWorks() throws JsonProcessingException {
    final EmbeddedVariable embeddedVariable = new EmbeddedVariable();
    embeddedVariable.setKey(KEY);
    embeddedVariable.setAsJson(true);
    embeddedVariable.setAsTransient(true);
    embeddedVariable.setSpin(true);
    embeddedVariable.setType(VariableType.LOCAL);

    when(outputVariable.getValue(any())).thenReturn(objectMapper.writeValueAsString(embeddedVariable));
    setField(abstractWorkflowIODelegate, "outputVariable", outputVariable);
    setField(abstractWorkflowIODelegate, "objectMapper", objectMapper);

    final EmbeddedVariable responseVariable = abstractWorkflowIODelegate.getOutputVariable(delegateExecution);

    assertEquals(embeddedVariable.getKey(), responseVariable.getKey());
    assertEquals(embeddedVariable.getAsJson(), responseVariable.getAsJson());
    assertEquals(embeddedVariable.getAsTransient(), responseVariable.getAsTransient());
    assertEquals(Boolean.TRUE.equals(embeddedVariable.getSpin()), Boolean.TRUE.equals(responseVariable.getSpin()));
    assertEquals(embeddedVariable.getType(), responseVariable.getType());
  }

  @Test
  void testHasOutputVariablesReturnsTrue() {
    when(inputVariables.getValue(any())).thenReturn(JSON_OBJECT);
    setField(abstractWorkflowIODelegate, "inputVariables", inputVariables);

    assertTrue(abstractWorkflowIODelegate.hasInputVariables(delegateExecution));
  }

  @Test
  void testHasOutputVariableReturnsTrue() {
    when(outputVariable.getValue(any())).thenReturn(JSON_OBJECT);
    setField(abstractWorkflowIODelegate, "outputVariable", outputVariable);

    assertTrue(abstractWorkflowIODelegate.hasOutputVariable(delegateExecution));
  }

  @Test
  void testHasOutputVariableReturnsFalse() {
    when(outputVariable.getValue(any())).thenReturn(null);
    setField(abstractWorkflowIODelegate, "outputVariable", outputVariable);

    assertFalse(abstractWorkflowIODelegate.hasOutputVariable(delegateExecution));

    setField(abstractWorkflowIODelegate, "outputVariable", null);

    assertFalse(abstractWorkflowIODelegate.hasOutputVariable(delegateExecution));
  }

  @Test
  void testSetOutputVariableWorks() {
    setField(abstractWorkflowIODelegate, "outputVariable", null);

    abstractWorkflowIODelegate.setOutputVariable(outputVariable);
    assertEquals(outputVariable, getField(abstractWorkflowIODelegate, "outputVariable"));
  }

  private static class Impl extends AbstractWorkflowIODelegate {

    Impl() {
      super(null, null);
    }

    Impl(ObjectMapper objectMapper, RuntimeService runtimeService) {
      super(objectMapper, runtimeService);
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      // Do nothing.
    }

    @Override
    public Class<?> fromTask() {
      return null;
    }
  };

}
