package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptTaskDeserializeCodeFailureTest {

  private static final Exception EXCEPTION = new RuntimeException();

  @Test
  void scriptEngineLoadFailedWorksTest() {
      ScriptTaskDeserializeCodeFailure exception = Assertions.assertThrows(ScriptTaskDeserializeCodeFailure.class, () -> {
      throw new ScriptTaskDeserializeCodeFailure(UUID);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
  }

  @Test
  void scriptEngineLoadFailedWorksWithParameterTest() {
      ScriptTaskDeserializeCodeFailure exception = Assertions.assertThrows(ScriptTaskDeserializeCodeFailure.class, () -> {
      throw new ScriptTaskDeserializeCodeFailure(UUID, EXCEPTION);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
  }

}
