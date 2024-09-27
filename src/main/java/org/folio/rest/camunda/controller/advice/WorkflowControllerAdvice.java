package org.folio.rest.camunda.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.workflow.exception.SecureOperationException;
import org.folio.spring.web.model.response.ResponseErrors;
import org.folio.spring.web.utility.ErrorUtility;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WorkflowControllerAdvice {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(WorkflowAlreadyActiveException.class)
  public ResponseErrors handleWorkflowAlreadyActiveException(WorkflowAlreadyActiveException exception) {
    log.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(SecureOperationException.class)
  public ResponseErrors handleSecureOperationException(SecureOperationException exception) {
    log.error(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
