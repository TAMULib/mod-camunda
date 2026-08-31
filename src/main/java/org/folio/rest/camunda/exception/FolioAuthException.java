package org.folio.rest.camunda.exception;

/**
 * An exception for when something goes wrong while getting or processing FOLIO authentication information.
 */
public class FolioAuthException extends RuntimeException {

  private static final String MESSAGE = "FOLIO Authentication Error: %s.";

  private static final long serialVersionUID = 3047885452810720087L;

  /**
   * Throw an exception with a message.
   *
   * @param reason A description of the reason for the exception.
   */
  public FolioAuthException(String reason) {

    super(String.format(MESSAGE, reason));
  }

  /**
   * Throw an exception with a message with a stack trace.
   *
   * @param reason A description of the reason for the exception.
   * @param e      A nested exception to associate.
   */
  public FolioAuthException(String reason, Exception e) {

    super(String.format(MESSAGE, reason), e);
  }

}
