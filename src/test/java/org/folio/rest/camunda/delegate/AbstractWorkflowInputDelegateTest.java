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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
class AbstractWorkflowInputDelegateTest {

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression inputVariables;

  @InjectMocks
  private ObjectMapper objectMapper;

  @Spy
  private Impl abstractDatabaseInputDelegate;

  @Test
  void testGetInputVariableWorks() throws JsonProcessingException {
    final EmbeddedVariable embeddedVariable = new EmbeddedVariable();
    embeddedVariable.setKey(KEY);
    embeddedVariable.setAsJson(true);
    embeddedVariable.setAsTransient(true);
    embeddedVariable.setSpin(true);
    embeddedVariable.setType(VariableType.LOCAL);
    Set<EmbeddedVariable> variables = new HashSet<>(List.of(embeddedVariable));

    when(inputVariables.getValue(any())).thenReturn(objectMapper.writeValueAsString(variables));
    setField(abstractDatabaseInputDelegate, "inputVariables", inputVariables);
    setField(abstractDatabaseInputDelegate, "objectMapper", objectMapper);

    final Set<EmbeddedVariable> responseVariables = abstractDatabaseInputDelegate.getInputVariables(delegateExecution);

    assertEquals(variables.size(), responseVariables.size());

    variables.forEach(ev -> {
      assertEquals(embeddedVariable.getKey(), ev.getKey());
      assertEquals(embeddedVariable.getKey(), ev.getKey());
      assertEquals(embeddedVariable.getAsJson(), ev.getAsJson());
      assertEquals(embeddedVariable.getAsTransient(), ev.getAsTransient());
      assertEquals(Boolean.TRUE.equals(embeddedVariable.getSpin()), Boolean.TRUE.equals(ev.getSpin()));
      assertEquals(embeddedVariable.getType(), ev.getType());
    });
  }

  @Test
  void testHasInputVariablesReturnsTrue() {
    when(inputVariables.getValue(any())).thenReturn(JSON_OBJECT);
    setField(abstractDatabaseInputDelegate, "inputVariables", inputVariables);

    assertTrue(abstractDatabaseInputDelegate.hasInputVariables(delegateExecution));
  }

  @Test
  void testHasInputVariablesReturnsFalse() {
    when(inputVariables.getValue(any())).thenReturn(null);
    setField(abstractDatabaseInputDelegate, "inputVariables", inputVariables);

    assertFalse(abstractDatabaseInputDelegate.hasInputVariables(delegateExecution));

    setField(abstractDatabaseInputDelegate, "inputVariables", null);

    assertFalse(abstractDatabaseInputDelegate.hasInputVariables(delegateExecution));
  }

  @Test
  void testSetInputVariableWorks() {
    setField(abstractDatabaseInputDelegate, "inputVariables", null);

    abstractDatabaseInputDelegate.setInputVariables(inputVariables);
    assertEquals(inputVariables, getField(abstractDatabaseInputDelegate, "inputVariables"));
  }

  private static class Impl extends AbstractWorkflowInputDelegate {

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
