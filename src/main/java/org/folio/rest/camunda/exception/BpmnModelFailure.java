package org.folio.rest.camunda.exception;

/**
 * Failure in the BpmnModel.
 */
public class BpmnModelFailure extends RuntimeException {

  private static final long serialVersionUID = 106282822928897620L;

  public BpmnModelFailure(String message) {
    super(message);
  }

  public BpmnModelFailure(String message, Exception e) {
    super(message, e);
  }

}