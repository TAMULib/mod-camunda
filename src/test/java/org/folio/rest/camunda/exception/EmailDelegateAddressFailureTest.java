package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.ID;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailDelegateAddressFailureTest {

  @Test
  void emailDelegateAddressFailureWorksTest() throws IOException {
    EmailDelegateAddressFailure exception = Assertions.assertThrows(EmailDelegateAddressFailure.class, () -> {
      throw new EmailDelegateAddressFailure(VALUE, UUID, ID);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(ID));
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(VALUE));
  }

  @Test
  void delegateSpinFailureWorksWorksWithParameterTest() throws IOException {
    EmailDelegateAddressFailure exception = Assertions.assertThrows(EmailDelegateAddressFailure.class, () -> {
      throw new EmailDelegateAddressFailure(VALUE, UUID, ID, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(ID));
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(VALUE));
  }

}
