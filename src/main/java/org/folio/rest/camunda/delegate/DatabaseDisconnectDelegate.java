package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.DatabaseDisconnectTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DatabaseDisconnectDelegate extends AbstractDatabaseDelegate {

  /**
   * Initializer.
   */
  public DatabaseDisconnectDelegate(ObjectMapper objectMapper, RuntimeService runtimeService, DatabaseConnectionService connectionService) {

    super(objectMapper, runtimeService, connectionService);
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    String key = this.designation.getValue(execution).toString();

    connectionService.destroyConnection(key);

    determineEndTime(execution, startTime);
  }

  @Override
  public Class<?> fromTask() {
    return DatabaseDisconnectTask.class;
  }

}
