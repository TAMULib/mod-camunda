package org.folio.rest.camunda.exception;

public class EmailDelegateAddressFailure extends RuntimeException {

  private static final long serialVersionUID = 42856034298853195L;

  private static final String MESSAGE = "Failed to attach %s e-mail address %s, reason: %s.";

  public EmailDelegateAddressFailure(String toFrom, String address, String reason) {
    super(String.format(MESSAGE, toFrom, address, reason));
  }

  public EmailDelegateAddressFailure(String toFrom, String address, String reason, Exception e) {
    super(String.format(MESSAGE, toFrom, address, reason), e);
  }

}
