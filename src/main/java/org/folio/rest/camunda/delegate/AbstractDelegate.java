package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDelegate implements JavaDelegate {

  private final Logger log;

  protected ObjectMapper objectMapper;

  protected RuntimeService runtimeService;

  /**
   * Initializer.
   */
  AbstractDelegate(ObjectMapper objectMapper, RuntimeService runtimeService) {

    this.objectMapper = objectMapper;
    this.runtimeService = runtimeService;

    // The logger is non-static to ensure that the implementing class name is used for the logger.
    log = LoggerFactory.getLogger(this.getClass());
  }

  /**
   * Get the delegate class name.
   *
   * @return The delegate name.
   */
  protected String getDelegateClass() {
    String simpleName = getClass().getSimpleName();

    return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
  }

  /**
   * Get the delegate name.
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
    return String.format("${%s}", getDelegateClass());
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

}
