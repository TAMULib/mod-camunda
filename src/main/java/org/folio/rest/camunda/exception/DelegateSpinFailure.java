package org.folio.rest.camunda.exception;

public class DelegateSpinFailure extends RuntimeException {

  private static final long serialVersionUID = -6270663785866339965L;

  private static final String MESSAGE = "Failed to spin key %s for %s.";

  public DelegateSpinFailure(String key, String delegate) {
    super(String.format(MESSAGE, key, delegate));
  }

  public DelegateSpinFailure(String key, String delegate, Exception e) {
    super(String.format(MESSAGE, key, delegate), e);
  }

}
