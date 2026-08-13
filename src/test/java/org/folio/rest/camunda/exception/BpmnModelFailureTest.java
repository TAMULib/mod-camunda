package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BpmnModelFailureTest {

  private static final Exception EXCEPTION = new RuntimeException();

  @Test
  void failureWorksTest() {
    BpmnModelFailure exception = Assertions.assertThrows(BpmnModelFailure.class, () -> {
      throw new BpmnModelFailure(VALUE);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(VALUE));
  }

  @Test
  void failureWorksWithParameterTest() {
    BpmnModelFailure exception = Assertions.assertThrows(BpmnModelFailure.class, () -> {
      throw new BpmnModelFailure(VALUE, EXCEPTION);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(VALUE));
  }

}
