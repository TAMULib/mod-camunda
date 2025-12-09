package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptEngineLoadFailedTest {

  @Test
  void scriptEngineLoadFailedWorksTest() throws IOException {
    ScriptEngineLoadFailed exception = Assertions.assertThrows(ScriptEngineLoadFailed.class, () -> {
      throw new ScriptEngineLoadFailed(VALUE);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(VALUE));
  }

  @Test
  void scriptEngineLoadFailedWorksWithParameterTest() throws IOException {
    ScriptEngineLoadFailed exception = Assertions.assertThrows(ScriptEngineLoadFailed.class, () -> {
      throw new ScriptEngineLoadFailed(VALUE, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(VALUE));
  }

}
