package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.KEY;
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
class DelegateMissingRequiredPropertyTest {

  @Test
  void delegateMissingRequiredPropertyWorksTest() throws IOException {
    DelegateMissingRequiredProperty exception = Assertions.assertThrows(DelegateMissingRequiredProperty.class, () -> {
      throw new DelegateMissingRequiredProperty(KEY, UUID, VALUE);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
  }

  @Test
  void delegateMissingRequiredPropertyWorksWithParameterTest() throws IOException {
    DelegateMissingRequiredProperty exception = Assertions.assertThrows(DelegateMissingRequiredProperty.class, () -> {
      throw new DelegateMissingRequiredProperty(KEY, UUID, VALUE, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
  }

}
