package org.folio.rest.camunda;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringContextAccessor is a utility class that provides access to the Spring
 * application context.
 *
 * It implements ApplicationContextAware to automatically set the application
 * context when the Spring container starts.
 */
@Component
public class SpringContextAccessor implements ApplicationContextAware {

  private static ApplicationContext context;

  /**
   * Sets the application context. This method is called by the Spring framework
   * when the application context is initialized.
   *
   * @param applicationContext the application context to set
   * @throws BeansException if an error occurs while setting the application
   *                        context
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  /**
   * Retrieves a bean from the application context by its class type.
   *
   * @param <T>       the type of the bean to retrieve
   * @param beanClass the class of the bean to retrieve
   * @return the bean instance
   */
  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

}
