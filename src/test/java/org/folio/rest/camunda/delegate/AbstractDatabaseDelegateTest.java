package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractDatabaseDelegateTest {

  @Mock
  private Expression designation;

  @Spy
  private Impl abstractDatabaseDelegate;

  @Test
  void setMessageWorksTest() {
    setField(abstractDatabaseDelegate, "designation", null);

    abstractDatabaseDelegate.setDesignation(designation);
    assertEquals(designation, getField(abstractDatabaseDelegate, "designation"));
  }

  private static class Impl extends AbstractDatabaseDelegate {

    Impl() {
      super(null, null, null);
    }

    Impl(ObjectMapper objectMapper, RuntimeService runtimeService, DatabaseConnectionService connectionService) {
      super(objectMapper, runtimeService, connectionService);
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
    }

    @Override
    public Class<?> fromTask() {
      return null;
    }
  };

}
