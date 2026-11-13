package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.sql.SQLException;
import java.util.Properties;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.DatabaseConnectionTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.Expression;
import org.operaton.bpm.model.bpmn.instance.FlowElement;

@ExtendWith(MockitoExtension.class)
class FolioDatabaseConnectionDelegateTest {

  private static final String CONNECTIONSERVICE = "connectionService";

  private static final String DESIGNATION = "designation";

  private static final String PASSWORD = "password";

  private static final String SEARCHPATH = "searchPath";

  private static final String URL = "url";

  private static final String USERNAME = "username";

  @Mock
  private DatabaseConnectionService connectionService;

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression designationExpression;

  @Mock
  private FlowElement flowElementBpmn;

  @Mock
  private Expression genericExpression;

  @Mock
  private Expression passwordExpression;

  @Mock
  private Expression searchPathExpression;

  @Mock
  private Expression urlExpression;

  @Mock
  private Expression usernameExpression;

  @InjectMocks
  private FolioDatabaseConnectionDelegate delegate;

  @Test
  void testExecuteWorks() throws Exception {

    setupExecuteMocking();

    doNothing().when(connectionService).createPool(anyBoolean(), any(Properties.class));

    delegate.execute(delegateExecution);

    verify(connectionService).createPool(anyBoolean(), any(Properties.class));
  }

  @Test
  void testExecuteThrowsException() {

    setupExecuteMocking();

    doThrow(SQLException.class).when(connectionService).createPool(anyBoolean(), any(Properties.class));

    assertThrows(Exception.class, () -> {
      delegate.execute(delegateExecution);
    });
  }

  @Test
  void testSetPasswordWorks() {

    setField(delegate, PASSWORD, null);

    delegate.setPassword(genericExpression);
    assertEquals(genericExpression, getField(delegate, PASSWORD));
  }

  @Test
  void testSetSearchPathWorks() {

    setField(delegate, SEARCHPATH, null);

    delegate.setSearchPath(genericExpression);
    assertEquals(genericExpression, getField(delegate,SEARCHPATH));
  }

  @Test
  void testSetUrlWorks() {

    setField(delegate, URL, null);

    delegate.setUrl(genericExpression);
    assertEquals(genericExpression, getField(delegate, URL));
  }

  @Test
  void testSetUsernameWorks() {

    setField(delegate, USERNAME, null);

    delegate.setUsername(genericExpression);
    assertEquals(genericExpression, getField(delegate, USERNAME));
  }

  @Test
  void testFromTaskWorks() {

    assertEquals(DatabaseConnectionTask.class, delegate.fromTask());
  }

  /**
   * Provide common mocking behavior for the execute() method.
   */
  private void setupExecuteMocking() {

    when(flowElementBpmn.getName()).thenReturn(KEY);
    when(delegateExecution.getBpmnModelElementInstance()).thenReturn(flowElementBpmn);
    when(designationExpression.getValue(any())).thenReturn(VALUE);
    when(passwordExpression.getValue(any())).thenReturn(PASSWORD);
    when(searchPathExpression.getValue(any())).thenReturn(SEARCHPATH);
    when(urlExpression.getValue(any())).thenReturn(URL);
    when(usernameExpression.getValue(any())).thenReturn(USERNAME);

    setField(delegate, CONNECTIONSERVICE, connectionService);
    setField(delegate, DESIGNATION,       designationExpression);
    setField(delegate, PASSWORD,          passwordExpression);
    setField(delegate, SEARCHPATH,        searchPathExpression);
    setField(delegate, USERNAME,          usernameExpression);
    setField(delegate, URL,               urlExpression);
  }
}
