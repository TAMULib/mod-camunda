package org.folio.rest.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDatabaseOutputDelegate extends AbstractDelegate implements Output {

  Expression identifier;

  private Expression outputVariable;

  @Autowired
  DatabaseConnectionService connectionService;

  public void setIdentifier(Expression identifier) {
    this.identifier = identifier;
  }

  public EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException {
    return objectMapper.readValue(outputVariable.getValue(execution).toString(), EmbeddedVariable.class);
  }

  public void setOutputVariable(Expression outputVariable) {
    this.outputVariable = outputVariable;
  }

}
