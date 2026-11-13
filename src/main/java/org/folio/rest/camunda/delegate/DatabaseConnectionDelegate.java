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
import org.folio.rest.workflow.model.DatabaseConnectionTask;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.Expression;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Database connection delegate.
 */
@Service
@Scope("prototype")
public class DatabaseConnectionDelegate extends AbstractDatabaseDelegate {

  protected final TenantProvisioningService tenantProvisioningService;

  protected Expression password;

  protected Expression searchPath;

  protected Expression url;

  protected Expression username;

  /**
   * Initializer.
   */
  public DatabaseConnectionDelegate(JsonMapper mapper, RuntimeService runtimeService, DatabaseConnectionService connectionService, TenantProvisioningService tenantProvisioningService) {

    super(mapper, runtimeService, connectionService);
    this.tenantProvisioningService = tenantProvisioningService;
  }

  /**
   * Get the task class associated with this delegate.
   *
   * @param The class task.
   */
  @Override
  public Class<?> fromTask() {

    return DatabaseConnectionTask.class;
  }

  /**
   * Set the pass word.
   *
   * @param password The pass word.
   */
  public void setPassword(Expression password) {

    this.password = password;
  }

  /**
   * Set the SQL search path.
   *
   * @param searchPath The SQL search path.
   */
  public void setSearchPath(Expression searchPath) {

    this.searchPath = searchPath;
  }

  /**
   * Set the URL.
   *
   * @param url The URL.
   */
  public void setUrl(Expression url) {

    this.url = url;
  }

  /**
   * Set the user name.
   *
   * @param username The user name.
   */
  public void setUsername(Expression username) {

    this.username = username;
  }

  /**
   * Construct the properties.
   *
   * @param execution     The delegate execution data.
   * @param searchPathExp The search path string (optional).
   *
   * @return The constructed properties.
   */
  protected Properties buildProperties(DelegateExecution execution, String searchPathString) {

    final Properties properties = new Properties();
    properties.setProperty(POOL_ID, this.designation.getValue(execution).toString());
    properties.setProperty(POOL_PASS, this.password.getValue(execution).toString());
    properties.setProperty(POOL_SEARCH_PATH, searchPathString);
    properties.setProperty(POOL_TENANT, execution.getTenantId());
    properties.setProperty(POOL_URL, this.url.getValue(execution).toString());
    properties.setProperty(POOL_USER, this.username.getValue(execution).toString());

    return properties;
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

    final Object searchPathExpr = this.searchPath.getValue(execution);

    final String searchPathString = searchPathExpr == null
      ? null
      : searchPathExpr.toString();

    final Properties properties = buildProperties(execution, searchPathString);

    try {
      tenantProvisioningService.ensureSchema(execution.getTenantId());
      connectionService.createPool(false, properties);
    } catch (Exception e) {
      throw new DelegateExecutionFailure(name, id, e.getMessage(), e);
    }
  }

}
