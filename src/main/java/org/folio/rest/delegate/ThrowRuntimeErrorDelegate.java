package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class ThrowRuntimeErrorDelegate extends AbstractLoggingJavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    try {
      log.info("Throwing runtime error...");
      throw new RuntimeException("This is an example exception!");
    } catch (Exception e) {
      String message = e.getMessage();
      throw new BpmnError("RUNTIME_ERROR", message);
    }
  }

}
