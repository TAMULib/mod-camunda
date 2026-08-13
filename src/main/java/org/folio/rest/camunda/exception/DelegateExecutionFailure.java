package org.folio.rest.camunda.exception;

public class DelegateExecutionFailure extends RuntimeException {

  private static final long serialVersionUID = 4525720846209903267L;

  private static final String MESSAGE = "Failed to execute delegate %s (ID %s), reason: %s.";

  public DelegateExecutionFailure(String name, String id, String reason) {
    super(String.format(MESSAGE, name, id, reason));
  }

  public DelegateExecutionFailure(String name, String id, String reason, Exception e) {
    super(String.format(MESSAGE, name, id, reason), e);
  }

}