package org.folio.rest.camunda.exception;

public class WorkflowAlreadyActiveException extends Exception {

  private static final long serialVersionUID = -7896387728725860219L;

  private static final String WORKFLOW_ALREADY_ACTIVE_MESSAGE = "The workflow: %s, is already active.";

  public WorkflowAlreadyActiveException(String id) {
    super(String.format(WORKFLOW_ALREADY_ACTIVE_MESSAGE, id));
  }

  public WorkflowAlreadyActiveException(String id, Exception e) {
    super(String.format(WORKFLOW_ALREADY_ACTIVE_MESSAGE, id), e);
  }

}
