package org.folio.rest.camunda.utility;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Provide the context for the MappingUtility class to use when performing operations from static methods.
 */
@Component
public class MappingUtilityContext implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setApplicationContext(ApplicationContext ctx) {
    applicationContext = ctx;
  }

  /**
   * Get the bean with the auto-wired variables.
   *
   * @param <T>   The type.
   * @param clazz The class.
   *
   * @return The bean from the context.
   */
  public static <T> T getBean(Class<T> clazz) {
    return applicationContext.getBean(clazz);
  }

}
