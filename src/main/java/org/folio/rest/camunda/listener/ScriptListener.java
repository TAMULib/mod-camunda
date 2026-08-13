package org.folio.rest.camunda.listener;

import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.folio.rest.camunda.config.FolioEnvConfig;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ScriptListener implements ExecutionListener {

  private FolioEnvConfig folioEnvConfig;

  /**
   * Initializer.
   */
  public ScriptListener(FolioEnvConfig folioEnvConfig) {

    this.folioEnvConfig = folioEnvConfig;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {

    final List<FolioEnvDefaultsItem> defaults = folioEnvConfig.getDefaults();

    if (defaults != null) {
      folioEnvConfig.getDefaults().forEach(item -> processEnvConfig(execution, item));
    }
  }

  /**
   * Process the environment variable configuration.
   *
   * This will remove any variable with expose set to false.
   *
   * @param execution The delegate execution data.
   */
  void processEnvConfig(final DelegateExecution execution, final FolioEnvDefaultsItem item) {

    if (Boolean.FALSE.equals(item.getExpose())) {
      final String name = item.getName();

      if (execution.hasVariable(name)) {
        execution.removeVariable(name);
      }

      if (execution.hasVariableLocal(name)) {
        execution.removeVariableLocal(name);
      }
    }
  }

}
