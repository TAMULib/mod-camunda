package org.folio.rest.camunda.delegate;

import static org.camunda.spin.Spin.JSON;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.camunda.config.FolioEnvConfig;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SetupDelegate extends AbstractDelegate {

  private static final String TIMESTAMP_VARIABLE_NAME = "timestamp";
  private static final String TENANT_VARIABLE_NAME = "tenantId";

  private ScriptEngineService scriptEngineService;

  private FolioEnvConfig folioEnvConfig;

  private Expression initialContext;

  private Expression processors;

  /**
   * Initializer.
   */
  public SetupDelegate(ObjectMapper objectMapper, RuntimeService runtimeService, FolioEnvConfig folioEnvConfig, ScriptEngineService scriptEngineService) {

    super(objectMapper, runtimeService);

    this.folioEnvConfig = folioEnvConfig;
    this.scriptEngineService = scriptEngineService;
  }

  public void setInitialContext(Expression initialContext) {
    this.initialContext = initialContext;
  }

  public void setProcessors(Expression processors) {
    this.processors = processors;
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    getLogger().info("loading initial context");

    Map<String, Object> context = objectMapper.readValue(initialContext.getValue(execution).toString(),
      new TypeReference<Map<String, Object>>() {
    });

    for (Map.Entry<String, Object> entry : context.entrySet()) {
      SpinJsonNode node = JSON(objectMapper.writeValueAsString(entry.getValue()));
      execution.setVariable(entry.getKey(), node);
      getLogger().info("{}: {}", entry.getKey(), node);
    }

    String timestamp = String.valueOf(System.currentTimeMillis());

    execution.setVariable(TIMESTAMP_VARIABLE_NAME, timestamp);
    execution.setVariable(TENANT_VARIABLE_NAME, execution.getTenantId());

    loadEnvConfig(execution);

    getLogger().info("loading scripts");

    List<EmbeddedProcessor> processorsValue = objectMapper.readValue(this.processors.getValue(execution).toString(),
      new TypeReference<List<EmbeddedProcessor>>() {
    });

    for (EmbeddedProcessor processor : processorsValue) {
      String extension = processor.getScriptType().getExtension();
      String functionName = processor.getFunctionName();
      String code = processor.getCode();
      scriptEngineService.registerScript(extension, functionName, code);
      getLogger().info("{}: {}", processor.getFunctionName(), processor.getCode());
    }

    determineEndTime(execution, startTime);
  }

  /**
   * Load the environment variable configuration.
   *
   * All variables are loaded and exported to the Operaton engine.
   *
   * @param execution The delegate execution data.
   */
  private void loadEnvConfig(final DelegateExecution execution) {

    final List<FolioEnvDefaultsItem> defaults = folioEnvConfig.getDefaults();

    if (defaults != null) {
      defaults.forEach(item -> {
        switch (item.getType()) {
          case LITERAL -> loadEnvConfigItemNormal(execution, item);
          case SECURE -> loadEnvConfigItemSecure(execution, item);
          case URL -> loadEnvConfigItemNormal(execution, item);
          case URL_PATH -> loadEnvConfigItemNormal(execution, item);
        }
      });
    }
  }

  /**
   * Handle normal item types.
   *
   * @param execution The delegate execution data.
   * @param item      The item to load into a Operaton variable.
   */
  private void loadEnvConfigItemNormal(final DelegateExecution execution, final FolioEnvDefaultsItem item) {

    execution.setVariable(item.getName(), item.getValue());
  }

  /**
   * Handle the SECURE item type.
   *
   * Secure variables have their values stored in Java memory rather than Operaton.
   * If the variable already exists, then delete it at the start, unless exposed.
   *
   * If exposed, then extract it and add it as a variable.
   *
   * @param execution The delegate execution data.
   * @param item      The item to load into a Operaton variable.
   */
  private void loadEnvConfigItemSecure(final DelegateExecution execution, final FolioEnvDefaultsItem item) {

    if (Boolean.TRUE.equals(item.getExpose())) {
      if (!execution.hasVariable(item.getName())) {
        item.getSecure(chars -> execution.setVariable(item.getName(), new String(chars)));
      }
    } else if (execution.hasVariable(item.getName())) {
      execution.removeVariable(item.getName());
    }
  }

}
