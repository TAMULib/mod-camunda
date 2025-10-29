package org.folio.rest.camunda.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.folio.spring.tenant.exception.NoTenantException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantInjectionDelegateAspect {

  private TenantProperties tenantProperties;

  public TenantInjectionDelegateAspect(TenantProperties tenantProperties) {
    this.tenantProperties = tenantProperties;
  }

  @Before("execution(* org.camunda.bpm.engine.delegate.JavaDelegate.execute (org.camunda.bpm.engine.delegate.DelegateExecution)) && args(execution)")
  public void beforeDelegateExecution(DelegateExecution execution) {
    setTenant(execution.getTenantId());
  }

  @Before("execution(* org.camunda.bpm.engine.delegate.ExecutionListener.notify (org.camunda.bpm.engine.delegate.DelegateExecution)) && args(execution)")
  public void beforeExecutionListenerNotify(DelegateExecution execution) {
    setTenant(execution.getTenantId());
  }

  @Before("execution(* org.camunda.bpm.engine.delegate.TaskListener.notify (org.camunda.bpm.engine.delegate.DelegateTask)) && args(task)")
  public void beforeTaskListenerNotify(DelegateTask task) {
    setTenant(task.getTenantId());
  }

  private void setTenant(String tenant) {
    if (tenant == null) {
      if (tenantProperties.isForceTenant()) {
        throw new NoTenantException("No tenant found in thread safe tenant storage!");
      }
      tenant = tenantProperties.getDefaultTenant();
    }
    ThreadLocalStorage.setTenant(tenant);
  }

}
