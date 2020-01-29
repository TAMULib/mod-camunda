package org.folio.rest.delegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.VariableType;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Input {

  public abstract Logger getLogger();

  public abstract ObjectMapper getObjectMapper();

  public abstract Set<EmbeddedVariable> getInputVariables(DelegateExecution execution) throws JsonProcessingException;

  public abstract void setInputVariables(Expression inputVariables);

  public default Map<String, Object> getInputs(DelegateExecution execution) throws JsonProcessingException {
    Map<String, Object> inputs = new HashMap<String, Object>();
    getInputVariables(execution).forEach(variable -> {
      Optional<String> key = variable.getKey();
      if (key.isPresent()) {
        Optional<VariableType> type = variable.getType();
        if (type.isPresent()) {
          Optional<Object> value = Optional.empty();
          switch (type.get()) {
          case LOCAL:
            value = Optional.ofNullable(execution.getVariableLocal(key.get()));
            break;
          case PROCESS:
            value = Optional.ofNullable(execution.getVariable(key.get()));
            break;
          default:
            break;
          }
          if (value.isPresent()) {
            if (variable.isSpin()) {
              JacksonJsonNode node = (JacksonJsonNode) value.get();
              if (node.isObject()) {
                inputs.put(key.get(), getObjectMapper().convertValue(((JacksonJsonNode) value.get()).unwrap(),
                    new TypeReference<Map<String, Object>>() {
                    }));
              } else if (node.isArray()) {
                inputs.put(key.get(), getObjectMapper().convertValue(((JacksonJsonNode) value.get()).unwrap(),
                    new TypeReference<List<Object>>() {
                    }));
              } else if (node.isValue()) {
                inputs.put(key.get(), node.value());
              }
            } else {
              inputs.put(key.get(), value.get());
            }
          } else {
            getLogger().warn("Could not find value for {} from {}", key, type);
          }
        } else {
          getLogger().warn("Variable type not present for {}", key.get());
        }
      } else {
        getLogger().warn("Output key is null");
      }
    });
    return inputs;
  }

}
