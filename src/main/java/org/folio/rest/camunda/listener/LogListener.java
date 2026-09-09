package org.folio.rest.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.folio.rest.camunda.service.CamundaApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * For providing a system log message for when the listener gets executed.
 */
@Service
@Scope("prototype")
public class LogListener implements ExecutionListener {

  private static final Logger LOG = LoggerFactory.getLogger(LogListener.class);

  @Override
  public void notify(DelegateExecution execution) throws Exception {

    final String activityId = execution.getCurrentActivityId();
    final String activityName = execution.getCurrentActivityName();
    final String processId = execution.getProcessDefinitionId();
    final String processName = execution.getProcessEngineServices()
      .getRepositoryService()
      .createProcessDefinitionQuery()
      .processDefinitionId(processId)
      .singleResult()
      .getName();

    LOG.info("Workflow '{}', Definition ID '{}', Activity '{}' ({}).", processName, processId, activityName, activityId);
  }

}
