package org.folio.rest.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.folio.rest.tenant.config.TenantConfig;
import org.folio.rest.tenant.exception.NoTenantException;
import org.folio.rest.tenant.storage.ThreadLocalStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantInjectionDelegateAspect {

  @Autowired
  private TenantConfig tenantConfig;

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
      if (tenantConfig.isForceTenant()) {
        throw new NoTenantException("No tenant found in thread safe tenant storage!");
      }
      tenant = tenantConfig.getDefaultTenant();
    }
    ThreadLocalStorage.setTenant(tenant);
  }

}
