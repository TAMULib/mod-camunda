package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDelegate implements JavaDelegate {

  private final Logger log;

  @Autowired
  protected ObjectMapper objectMapper;

  AbstractDelegate() {
    // The logger is non-static to ensure that the implementing class name is used for the logger.
    log = LoggerFactory.getLogger(this.getClass());
  }

  /**
   * Get the delegate name.
   *
   * This is used for functions that do not have the delegate execution data.
   * The delegate name is derived from the class name.
   *
   * @return The delegate name.
   */
  protected String getDelegateName() {
    String simpleName = getClass().getSimpleName();

    return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
  }

  /**
   * Get the delegate name.
   *
   * This is the preferred method to get the delegate name but requires access to the delegate execution.
   * The delegate execution data is used to derive the delegate name.
   *
   * @param execution The delegate execution data.
   *
   * @return The delegate name.
   */
  protected String getDelegateName(DelegateExecution execution) {
    return execution.getBpmnModelElementInstance().getName();
  }

  /**
   * Get the expression.
   *
   * @return The formatted expression string.
   */
  public String getExpression() {
    return String.format("${%s}", getDelegateName());
  }

  /**
   * Get the logger.
   *
   * @return The logger for this class.
   */
  public Logger getLogger() {
    return log;
  }

  /**
   * Get the Object Mapper.
   *
   * @return The Object Mapper for this class.
   */
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * Determine the start time of the query and print log.
   *
   * @param execution The delegate execution data.
   * @param args additional string arguments to pass.
   *
   * @return The start time.
   */
  protected long determineStartTime(DelegateExecution execution, Object ...args) {
    getLogger().info("{} {} started", getDelegateName(execution), args);

    return System.nanoTime();
  }

  /**
   * Given the start time, determine the total time spent.
   *
   * @param execution The delegate execution data.
   *
   * @param startTime The time the process started.
   */
  protected void determineEndTime(DelegateExecution execution, long startTime) {
    getLogger().info("{} finished in {} milliseconds", getDelegateName(execution), (System.nanoTime() - startTime) / (double) 1000000);
  }

  /**
   * Log that the delegate has started.
   *
   * @param execution The delegate execution data.
   *
   * @param startTime The time the process started.
   */
  protected void logExecutionStart(DelegateExecution execution) {
    getLogger().info("{} started", getDelegateName(execution));
  }

}
