package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.camunda.service.DatabaseConnectionService;

public abstract class AbstractDatabaseDelegate extends AbstractWorkflowDelegate {

  Expression designation;

  DatabaseConnectionService connectionService;

  /**
   * Initializer.
   */
  AbstractDatabaseDelegate(ObjectMapper objectMapper, RuntimeService runtimeService, DatabaseConnectionService connectionService) {

    super(objectMapper, runtimeService);

    this.connectionService = connectionService;
  }

  public void setDesignation(Expression designation) {
    this.designation = designation;
  }

}
