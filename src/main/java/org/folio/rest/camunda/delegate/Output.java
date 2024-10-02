package org.folio.rest.camunda.delegate;

import static org.camunda.spin.Spin.JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.variable.Variables;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.converter.CryptoConverter;
import org.slf4j.Logger;

public interface Output {

  public abstract Logger getLogger();

  public abstract ObjectMapper getObjectMapper();

  public abstract EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException;

  public abstract void setOutputVariable(Expression outputVariable);

  public abstract boolean hasOutputVariable(DelegateExecution execution);

  public default void setOutput(DelegateExecution execution, Object output) throws JsonProcessingException {
    if (!hasOutputVariable(execution)) {
      getLogger().warn("Output variable for execution {} is null", execution.getId());
      return;
    }

    EmbeddedVariable variable = getOutputVariable(execution);
    String key = variable.getKey();

    if (key == null) {
      getLogger().warn("Output key is null");
      return;
    }

    if (output == null) {
       getLogger().warn("Output not present for {}", key);
      return;
    }

    VariableType type = variable.getType();

    Object value = Boolean.TRUE.equals(variable.getSpin())
      ? JSON(getObjectMapper().writeValueAsString(output))
      : variable.getAsSecure() && String.class.isAssignableFrom(output.getClass())
        ? Variables.objectValue(CryptoConverter.encrypt((String) output), variable.getAsTransient()).create();
        : Variables.objectValue(output, variable.getAsTransient()).create();

    if (type == VariableType.LOCAL) {
      execution.setVariableLocal(key, value);
    } else if (type == VariableType.PROCESS) {
      execution.setVariable(key, value);
    } else {
      getLogger().warn("Variable type not present for {}", key);
    }
  }

}
