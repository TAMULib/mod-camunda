package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class AbstractDelegateTest {

  @Mock
  private ObjectMapper objectMapper;

  @Spy
  private Impl abstractDelegate;

  @Test
  void getExpressionWorksTest() {
    final String simpleName = Impl.class.getSimpleName();
    final String delegateName = "${" + simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1) + "}";

    assertEquals(delegateName, abstractDelegate.getExpression());
  }

  @Test
  void getLoggerWorksTest() {
    final Logger expectLog = LoggerFactory.getLogger(Impl.class);

    assertEquals(expectLog.getName(), abstractDelegate.getLogger().getName());
  }

  @Test
  void getObjectMapperWorksTest() {
    setField(abstractDelegate, "objectMapper", objectMapper);

    assertEquals(objectMapper, abstractDelegate.getObjectMapper());
  }

  private static class Impl extends AbstractDelegate {

    Impl() {
      super(null, null);
    }

    Impl(ObjectMapper objectMapper, RuntimeService runtimeService) {
      super(objectMapper, runtimeService);
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      // Do nothing.
    }
  }

}
