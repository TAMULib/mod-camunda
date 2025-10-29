package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.ID;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelegateSpinFailureTest {

  @Test
  void delegateSpinFailureWorksTest() throws IOException {
    DelegateSpinFailure exception = Assertions.assertThrows(DelegateSpinFailure.class, () -> {
      throw new DelegateSpinFailure(UUID, ID);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(ID));
  }

  @Test
  void delegateSpinFailureWorksWorksWithParameterTest() throws IOException {
    DelegateSpinFailure exception = Assertions.assertThrows(DelegateSpinFailure.class, () -> {
      throw new DelegateSpinFailure(UUID, ID, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(ID));
  }
}
