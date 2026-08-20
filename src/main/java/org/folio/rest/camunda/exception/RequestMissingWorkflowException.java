package org.folio.rest.camunda.exception;

/**
 * Designate a bad request due to missing Workflow.
 */
public class RequestMissingWorkflowException extends RuntimeException {

  private static final long serialVersionUID = 2234332019817219L;

  private static final String MSG = "No workflow has been specified for the %s request.";

  /**
   * Designate a bad request regarding the specified request end point.
   *
   * @param request The request end point.
   */
  public RequestMissingWorkflowException(String request) {
    super(String.format(MSG, request));
  }

  /**
   * Designate a bad request regarding the specified request end point.
   *
   * @param request The request end point.
   * @param e       An associated exception.
   */
  public RequestMissingWorkflowException(String request, Exception e) {
    super(String.format(MSG, request), e);
  }

}
