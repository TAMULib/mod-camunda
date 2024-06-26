package org.folio.rest.camunda.delegate;

import java.util.Objects;
import java.util.Set;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

public abstract class AbstractWorkflowInputDelegate extends AbstractWorkflowDelegate implements Input {

  private Expression inputVariables;

  protected AbstractWorkflowInputDelegate() {
    super();
  }

  public Set<EmbeddedVariable> getInputVariables(DelegateExecution execution) throws JsonProcessingException {
    return objectMapper.readValue(inputVariables.getValue(execution).toString(),
        new TypeReference<Set<EmbeddedVariable>>() {});
  }

  public boolean hasInputVariables(DelegateExecution execution) {
    return Objects.nonNull(inputVariables) && Objects.nonNull(inputVariables.getValue(execution));
  }

  public void setInputVariables(Expression inputVariables) {
    this.inputVariables = inputVariables;
  }

}
