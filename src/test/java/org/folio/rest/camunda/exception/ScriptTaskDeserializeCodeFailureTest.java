package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptTaskDeserializeCodeFailureTest {

  @Test
  void scriptEngineLoadFailedWorksTest() throws IOException {
      ScriptTaskDeserializeCodeFailure exception = Assertions.assertThrows(ScriptTaskDeserializeCodeFailure.class, () -> {
      throw new ScriptTaskDeserializeCodeFailure(UUID);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
  }

  @Test
  void scriptEngineLoadFailedWorksWithParameterTest() throws IOException {
      ScriptTaskDeserializeCodeFailure exception = Assertions.assertThrows(ScriptTaskDeserializeCodeFailure.class, () -> {
      throw new ScriptTaskDeserializeCodeFailure(UUID, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
  }

}
