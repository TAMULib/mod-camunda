package org.folio.rest.camunda.delegate;

import org.folio.rest.workflow.model.InputTask;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Scope("prototype")
public class InputDelegate extends AbstractWorkflowIODelegate {

  @Value("${folio.gateway.url}")
  private String okapiUrl;

  /**
   * Initializer.
   */
  public InputDelegate(JsonMapper mapper, RuntimeService runtimeService) {

    super(mapper, runtimeService);
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   * @param name      The delegate name.
   * @param id        The delegate ID.
   */
  @Override
  protected void performExecute(DelegateExecution execution, String name, String id) {
    // Should be empty.
  }

  @Override
  public Class<?> fromTask() {
    return InputTask.class;
  }

}
