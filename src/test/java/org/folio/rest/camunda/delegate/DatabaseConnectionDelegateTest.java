package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.URL;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.sql.SQLException;
import java.util.Properties;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.DatabaseConnectionTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionDelegateTest {

  private static final String USERNAME = "username";

  private static final String PASSWORD = "password";

  @Mock
  private DatabaseConnectionService connectionService;

  @Mock
  private FlowElement flowElementBpmn;

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression designationExpression;

  @Mock
  private Expression genericExpression;

  @Mock
  private Expression passwordExpression;

  @Mock
  private Expression urlExpression;

  @Mock
  private Expression usernameExpression;

  @InjectMocks
  private DatabaseConnectionDelegate databaseConnectionDelegate;

  @Test
  void testExecuteWorks() throws Exception {
    setupExecuteMocking();

    doNothing().when(connectionService).createPool(anyString(), anyString(), any(Properties.class));

    databaseConnectionDelegate.execute(delegateExecution);

    verify(connectionService).createPool(anyString(), anyString(), any(Properties.class));
  }

  @Test
  void testExecuteThrowsException() throws SQLException {
    setupExecuteMocking();

    doThrow(SQLException.class).when(connectionService).createPool(anyString(), anyString(), any(Properties.class));

    assertThrows(Exception.class, () -> {
      databaseConnectionDelegate.execute(delegateExecution);
    });
  }

  @Test
  void testSetUrlWorks() {
    setField(databaseConnectionDelegate, "url", null);

    databaseConnectionDelegate.setUrl(genericExpression);
    assertEquals(genericExpression, getField(databaseConnectionDelegate, "url"));
  }

  @Test
  void testSetUsernameWorks() {
    setField(databaseConnectionDelegate, "username", null);

    databaseConnectionDelegate.setUsername(genericExpression);
    assertEquals(genericExpression, getField(databaseConnectionDelegate, "username"));
  }

  @Test
  void testSetPasswordWorks() {
    setField(databaseConnectionDelegate, "password", null);

    databaseConnectionDelegate.setPassword(genericExpression);
    assertEquals(genericExpression, getField(databaseConnectionDelegate, "password"));
  }

  @Test
  void testFromTaskWorks() {
    assertEquals(DatabaseConnectionTask.class, databaseConnectionDelegate.fromTask());
  }

  /**
   * Provide common mocking behavior for the execute() method.
   */
  private void setupExecuteMocking() {
    when(delegateExecution.getBpmnModelElementInstance()).thenReturn(flowElementBpmn);
    when(flowElementBpmn.getName()).thenReturn(KEY);
    when(urlExpression.getValue(any())).thenReturn(URL);
    when(designationExpression.getValue(any())).thenReturn(VALUE);
    when(usernameExpression.getValue(any())).thenReturn(USERNAME);
    when(passwordExpression.getValue(any())).thenReturn(PASSWORD);

    setField(databaseConnectionDelegate, "url", urlExpression);
    setField(databaseConnectionDelegate, "designation", designationExpression);
    setField(databaseConnectionDelegate, "username", usernameExpression);
    setField(databaseConnectionDelegate, "password", passwordExpression);
    setField(databaseConnectionDelegate, "connectionService", connectionService);
  }
}
