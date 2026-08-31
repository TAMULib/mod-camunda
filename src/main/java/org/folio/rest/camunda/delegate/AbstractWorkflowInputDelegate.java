package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Set;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;

public abstract class AbstractWorkflowInputDelegate extends AbstractWorkflowDelegate implements Input {

  private Expression inputVariables;

  /**
   * Initializer.
   */
  AbstractWorkflowInputDelegate(ObjectMapper objectMapper, RuntimeService runtimeService) {

    super(objectMapper, runtimeService);
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
