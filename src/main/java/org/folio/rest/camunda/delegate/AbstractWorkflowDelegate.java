package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;

/**
 * Abstract workflow delegate.
 */
public abstract class AbstractWorkflowDelegate extends AbstractDelegate {

  public abstract Class<?> fromTask();

  /**
   * Initializer.
   */
  AbstractWorkflowDelegate(ObjectMapper objectMapper, RuntimeService runtimeService) {

    super(objectMapper, runtimeService);
  }

}
