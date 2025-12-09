package org.folio.rest.camunda.controller.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.rest.camunda.exception.DelegateSpinFailure;
import org.folio.rest.camunda.exception.EmailDelegateAddressFailure;
import org.folio.rest.camunda.exception.ScriptEngineLoadFailed;
import org.folio.rest.camunda.exception.ScriptEngineUnsupported;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalAdvice extends AbstractAdvice {

  ObjectMapper objectMapper;

  public GlobalAdvice() {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(DelegateSpinFailure.class)
  public ResponseEntity<String> handleDelegateSpinFailure(DelegateSpinFailure exception) {
    return buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(EmailDelegateAddressFailure.class)
  public ResponseEntity<String> handleEmailDelegateAddressFailure(EmailDelegateAddressFailure exception) {
    return buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(ScriptEngineLoadFailed.class)
  public ResponseEntity<String> handleScriptEngineLoadFailed(ScriptEngineLoadFailed exception) {
    return buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(ScriptEngineUnsupported.class)
  public ResponseEntity<String> handleScriptEngineUnsupported(ScriptEngineUnsupported exception) {
    return buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(WorkflowAlreadyActiveException.class)
  public ResponseEntity<String> handleWorkflowAlreadyActiveException(WorkflowAlreadyActiveException exception) {
    return buildError(exception, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);
  }

}
