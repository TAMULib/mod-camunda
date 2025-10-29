package org.folio.rest.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.workflow.model.InputTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class InputDelegate extends AbstractWorkflowIODelegate {

  @Value("${okapi.url}")
  private String okapiUrl;

  public InputDelegate() {
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    determineEndTime(execution, startTime);
  }

  @Override
  public Class<?> fromTask() {
    return InputTask.class;
  }

}
