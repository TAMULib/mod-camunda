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

  private static final String TIMESTAMP = "timestamp";
  private static final String TENANT_ID = "tenantId";

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

    execution.setVariable(TIMESTAMP, timestamp);
    execution.setVariable(TENANT_ID, execution.getTenantId());

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
   * The Tenant ID is also exposed.
   *
   * @param execution The delegate execution data.
   */
  private void loadEnvConfig(final DelegateExecution execution) {

    folioEnvConfig.eachItem((key, item) -> loadEnvConfigItem(execution, item));

    execution.setVariable(TENANT_ID, execution.getTenantId());
  }

  /**
   * Load items into the variables, but only if exposed.
   *
   * @param execution The delegate execution data.
   * @param item      The item to load into a Operaton variable.
   */
  private void loadEnvConfigItem(final DelegateExecution execution, final FolioEnvDefaultsItem item) {

    if (Boolean.TRUE.equals(item.getExpose())) {
      execution.setVariable(item.getName(), item.getValue());
    }
  }

}
