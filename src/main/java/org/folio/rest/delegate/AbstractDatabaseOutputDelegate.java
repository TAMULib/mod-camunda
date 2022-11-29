package org.folio.rest.delegate;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

// This class probably should be called AbstractDatabaseIODelegate to align with AbstractWorkflowIODelegate.
// Deferring refactor at this time in case it may cause breaking changes.
public abstract class AbstractDatabaseOutputDelegate extends AbstractWorkflowInputDelegate implements Output {

  Expression designation;

  private Expression outputVariable;

  @Autowired
  DatabaseConnectionService connectionService;

  public void setDesignation(Expression designation) {
    this.designation = designation;
  }

  public Boolean hasOutputVariable(DelegateExecution execution) {
    return Objects.nonNull(outputVariable) &&
        Objects.nonNull(outputVariable.getValue(execution));
  }

  public EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException {
    return objectMapper.readValue(outputVariable.getValue(execution).toString(), EmbeddedVariable.class);
  }

  public void setOutputVariable(Expression outputVariable) {
    this.outputVariable = outputVariable;
  }

}
