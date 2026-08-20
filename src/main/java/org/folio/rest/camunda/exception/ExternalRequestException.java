package org.folio.rest.camunda.exception;

/**
 * An exception for grouping FOLIO HTTP request specific exceptions.
 */
public class ExternalRequestException extends RuntimeException {

  /**
   * A generic message for a FOLIO HTTP request.
   */
  private static final String MESSAGE = "Bad External Request: %s.";

  private static final long serialVersionUID = 108615126277919338L;

  /**
   * Throw an exception with a message.
   *
   * @param reason A description of the reason for the exception.
   */
  public ExternalRequestException(String reason) {

    super(String.format(MESSAGE, reason));
  }

  /**
   * Throw an exception with a message with a stack trace.
   *
   * @param reason A description of the reason for the exception.
   * @param e      A nested exception to associate.
   */
  public ExternalRequestException(String reason, Exception e) {

    super(String.format(MESSAGE, reason), e);
  }

}
