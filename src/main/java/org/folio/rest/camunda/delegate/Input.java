package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.converter.CryptoConverter;
import org.slf4j.Logger;

public interface Input {

  public abstract Logger getLogger();

  public abstract ObjectMapper getObjectMapper();

  public abstract Set<EmbeddedVariable> getInputVariables(DelegateExecution execution) throws JsonProcessingException;

  public abstract boolean hasInputVariables(DelegateExecution execution);

  public abstract void setInputVariables(Expression inputVariables);

  public default Map<String, Object> getInputs(DelegateExecution execution) throws JsonProcessingException {
    Map<String, Object> inputs = new HashMap<>();

    if (!hasInputVariables(execution)) {
      getLogger().warn("Input variables for execution {} is null", execution.getId());
      return inputs;
    }

    for (EmbeddedVariable variable : getInputVariables(execution)) {
      String key = variable.getKey();
      VariableType type = variable.getType();

      if (key == null) {
        getLogger().warn("Input key is null");
      } else if (type == null) {
        getLogger().warn("Variable type not present for {}", key);
      } else if (type == VariableType.LOCAL || type == VariableType.PROCESS) {

        Object value = type == VariableType.LOCAL
          ? execution.getVariableLocal(key)
          : execution.getVariable(key);

        defaultGetInputsLoop(variable, key, type, value, inputs);
      } else {
        getLogger().warn("Could not find value for {} from {}", key, type);
      }
    }

    return inputs;
  }

  /**
   * Helper function for getInputs() to help solve "S3776" coding practice.
   *
   * @param variable The not-null variable.
   * @param key The not-null key.
   * @param type The not-null type.
   * @param value The not-null value.
   * @param inputs The inputs array to append the value to.
   *
   * @throws JsonProcessingException Failed to process JSON.
   */
  private void defaultGetInputsLoop(EmbeddedVariable variable, String key, VariableType type, Object value, Map<String, Object> inputs) throws JsonProcessingException {
    if (Boolean.FALSE.equals(variable.getSpin())) {
      inputs.put(key, value);
      return;
    }

    JacksonJsonNode node = (JacksonJsonNode) value;
    if (node == null) {
      getLogger().warn("Could not find node for value for {} from {}", key, type);
    } else if (Boolean.TRUE.equals(variable.getAsJson())) {
      inputs.put(key, getObjectMapper().writeValueAsString(node.unwrap()));
    } else if (node.isObject()) {
      inputs.put(key, getObjectMapper().convertValue(node.unwrap(), new TypeReference<Map<String, Object>>() {}));
    } else if (Boolean.TRUE.equals(node.isArray())) {
      inputs.put(key, getObjectMapper().convertValue(node.unwrap(), new TypeReference<List<Object>>() {}));
    } else if (Boolean.TRUE.equals(node.isValue())) {
      try {
        // Attempt to convert JSON String to Object (JsonNode)
        inputs.put(key, getObjectMapper().readTree((String) node.value()));
      } catch (Exception e) {
        if (variable.getAsSecure() && String.class.isAssignableFrom(value.getClass())) {
          inputs.put(key, CryptoConverter.decrypt((String) node.value()));
        } else {
          inputs.put(key, node.value());
        }
      }
    }
  }

}
