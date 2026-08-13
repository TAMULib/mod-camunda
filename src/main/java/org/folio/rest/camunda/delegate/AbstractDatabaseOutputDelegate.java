package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.EmbeddedVariable;

/**
 * This class probably should be called AbstractDatabaseIODelegate to align with AbstractWorkflowIODelegate.
 *
 * Deferring refactor at this time in case it may cause breaking changes.
 */
public abstract class AbstractDatabaseOutputDelegate extends AbstractWorkflowInputDelegate implements Output {

  Expression designation;

  private Expression outputVariable;

  DatabaseConnectionService connectionService;

  /**
   * Initializer.
   */
  AbstractDatabaseOutputDelegate(ObjectMapper objectMapper, RuntimeService runtimeService, DatabaseConnectionService connectionService) {

    super(objectMapper, runtimeService);

    this.connectionService = connectionService;
  }

  public void setDesignation(Expression designation) {
    this.designation = designation;
  }

  public boolean hasOutputVariable(DelegateExecution execution) {
    return Objects.nonNull(outputVariable) && Objects.nonNull(outputVariable.getValue(execution));
  }

  public EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException {
    return objectMapper.readValue(outputVariable.getValue(execution).toString(), EmbeddedVariable.class);
  }

  public void setOutputVariable(Expression outputVariable) {
    this.outputVariable = outputVariable;
  }

}
