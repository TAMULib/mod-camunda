package org.folio.rest.camunda.controller.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.rest.camunda.exception.DelegateSpinFailure;
import org.folio.rest.camunda.exception.EmailDelegateAddressFailure;
import org.folio.rest.camunda.exception.ScriptEngineLoadFailed;
import org.folio.rest.camunda.exception.ScriptEngineUnsupported;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GlobalAdviceTest {

  @Mock
  private DelegateSpinFailure delegateSpinFailure;

  @Mock
  private EmailDelegateAddressFailure emailDelegateAddressFailure;

  @Mock
  private ScriptEngineLoadFailed scriptEngineLoadFailed;

  @Mock
  private ScriptEngineUnsupported scriptEngineUnsupported;

  @Mock
  private WorkflowAlreadyActiveException workflowAlreadyActiveException;

  @Autowired
  private GlobalAdvice globalAdvice;

  @BeforeEach
  void beforeEach() {
    globalAdvice = new GlobalAdvice();
  }

  @Test
  void handleDelegateSpinFailureTest() {
    ResponseEntity<String> response = globalAdvice.handleDelegateSpinFailure(delegateSpinFailure);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleEmailDelegateAddressFailureTest() {
    ResponseEntity<String> response = globalAdvice.handleEmailDelegateAddressFailure(emailDelegateAddressFailure);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleScriptEngineLoadFailedTest() {
    ResponseEntity<String> response = globalAdvice.handleScriptEngineLoadFailed(scriptEngineLoadFailed);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleScriptEngineUnsupportedTest() {
    ResponseEntity<String> response = globalAdvice.handleScriptEngineUnsupported(scriptEngineUnsupported);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleWorkflowAlreadyActiveExceptionTest() {
    ResponseEntity<String> response = globalAdvice.handleWorkflowAlreadyActiveException(workflowAlreadyActiveException);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

}
