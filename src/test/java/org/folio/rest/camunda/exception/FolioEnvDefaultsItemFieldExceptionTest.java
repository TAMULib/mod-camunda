package org.folio.rest.camunda.exception;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.EXPOSE;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FolioEnvDefaultsItemFieldExceptionTest {

  private static final String MSG = "Example error message.";

  private static final Exception EXCEPTION = new RuntimeException();

  @Test
  void delegateExecutionFailureWorksTest() {
    FolioEnvDefaultsItemFieldException exception = Assertions.assertThrows(FolioEnvDefaultsItemFieldException.class, () -> {
      throw new FolioEnvDefaultsItemFieldException(EXPOSE, MSG);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(EXPOSE.getName()));
    assertTrue(exception.getMessage().contains(MSG));
  }

  @Test
  void delegateExecutionFailureWorksWithExceptionTest() {
    FolioEnvDefaultsItemFieldException exception = Assertions.assertThrows(FolioEnvDefaultsItemFieldException.class, () -> {
      throw new FolioEnvDefaultsItemFieldException(EXPOSE, MSG, EXCEPTION);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(EXPOSE.getName()));
    assertTrue(exception.getMessage().contains(MSG));
  }

  @Test
  void delegateExecutionFailureWithNameWorksTest() {
    FolioEnvDefaultsItemFieldException exception = Assertions.assertThrows(FolioEnvDefaultsItemFieldException.class, () -> {
      throw new FolioEnvDefaultsItemFieldException(EXPOSE, UUID, MSG);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(EXPOSE.getName()));
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(MSG));
  }

  @Test
  void delegateExecutionFailureWithNameWorksWithExceptionTest() {
    FolioEnvDefaultsItemFieldException exception = Assertions.assertThrows(FolioEnvDefaultsItemFieldException.class, () -> {
      throw new FolioEnvDefaultsItemFieldException(EXPOSE, MSG, UUID, EXCEPTION);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(EXPOSE.getName()));
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(MSG));
  }

}
