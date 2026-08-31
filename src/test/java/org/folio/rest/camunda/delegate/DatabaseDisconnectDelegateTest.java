package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.sql.SQLException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.DatabaseDisconnectTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseDisconnectDelegateTest {

  @Mock
  private DatabaseConnectionService connectionService;

  @Mock
  private FlowElement flowElementBpmn;

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression designationExpression;

  @InjectMocks
  private DatabaseDisconnectDelegate databaseDisconnectDelegate;

  @Test
  void testExecuteWorks() throws Exception {
    setupExecuteMocking();

    doNothing().when(connectionService).destroyConnection(anyString());

    databaseDisconnectDelegate.execute(delegateExecution);

    verify(connectionService).destroyConnection(anyString());
  }

  @Test
  void testExecuteThrowsException() throws SQLException {
    setupExecuteMocking();

    doThrow(SQLException.class).when(connectionService).destroyConnection(anyString());

    assertThrows(Exception.class, () -> {
      databaseDisconnectDelegate.execute(delegateExecution);
    });
  }

  @Test
  void testFromTaskWorks() {
    assertEquals(DatabaseDisconnectTask.class, databaseDisconnectDelegate.fromTask());
  }

  /**
   * Provide common mocking behavior for the execute() method.
   */
  private void setupExecuteMocking() {
    when(delegateExecution.getBpmnModelElementInstance()).thenReturn(flowElementBpmn);
    when(flowElementBpmn.getName()).thenReturn(KEY);
    when(designationExpression.getValue(any())).thenReturn(VALUE);

    setField(databaseDisconnectDelegate, "designation", designationExpression);
    setField(databaseDisconnectDelegate, "connectionService", connectionService);
  }
}
