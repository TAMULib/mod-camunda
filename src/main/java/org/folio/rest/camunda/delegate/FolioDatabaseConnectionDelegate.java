package org.folio.rest.camunda.delegate;

import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_ID;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_PASS;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_SEARCH_PATH;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_TENANT;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_URL;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_USER;

import java.util.Properties;
import org.folio.rest.camunda.exception.DelegateExecutionFailure;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.camunda.service.TenantProvisioningService;
import org.folio.rest.workflow.model.FolioDatabaseConnectionTask;
import org.folio.spring.tenant.service.SchemaService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.Expression;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Database connection delegate specific to FOLIO connections.
 *
 * This brings in more specialized and automatic functionality.
 */
@Service
@Scope("prototype")
public class FolioDatabaseConnectionDelegate extends DatabaseConnectionDelegate {

  private final SchemaService schemaService;

  /**
   * Initializer.
   */
  public FolioDatabaseConnectionDelegate(JsonMapper mapper, RuntimeService runtimeService, DatabaseConnectionService connectionService, SchemaService schemaService, TenantProvisioningService tenantProvisioningService) {

    super(mapper, runtimeService, connectionService, tenantProvisioningService);

    this.schemaService = schemaService;
  }

  /**
   * Get the task class associated with this delegate.
   *
   * @param The class task.
   */
  @Override
  public Class<?> fromTask() {

    return FolioDatabaseConnectionTask.class;
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

    final String tenant = execution.getTenantId();

    final Object searchPathExpr = this.searchPath.getValue(execution);

    final String searchPathString = searchPathExpr == null
      ? ""
      : searchPathExpr.toString();

    final String searchPathValue = searchPathString.isBlank()
      ? schemaService.getSchema(tenant) + ", public"
      : searchPathString;

    final Properties properties = buildProperties(execution, searchPathValue);

    try {
      tenantProvisioningService.ensureSchema(tenant);
      connectionService.createPool(true, properties);
    } catch (Exception e) {
      throw new DelegateExecutionFailure(name, id, e.getMessage(), e);
    }
  }

}
