package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.URL;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.cache.FolioTokenCache;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.folio.rest.workflow.dto.Request;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.FolioRequestTask;
import org.folio.spring.web.service.HttpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FolioRequestDelegateTest {

  private static final String TOKEN_HEADER_NAME = "X-Okapi-Token";

  @Mock
  private HttpService httpService;

  @Mock
  private FlowElement flowElementBpmn;

  @Mock
  private FolioEnvDefaultsItem folioEnvDefaultsItem;

  @Mock
  private FolioTokenCache folioTokenCache;

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression headerOutputVariablesExpression;

  @Mock
  private Expression requestExpression;

  @Mock
  private ResponseEntity<Object> responseEntity;

  @InjectMocks
  private FolioRequestDelegate delegate;

  private EmbeddedVariable embeddedVariable;

  private Set<EmbeddedVariable> embeddedVariables;

  private Request request;

  private String requestStr;

  private ObjectMapper mapper;

  private HttpHeaders httpHeaders;

  @BeforeEach
  void beforeEach() {
    mapper = new ObjectMapper();

    request = new Request();
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setAccept(MediaType.APPLICATION_JSON_VALUE);
    request.setMethod(HttpMethod.GET);
    request.setBodyTemplate(JSON_OBJECT);
    request.setUrl(URL);
    requestStr = "{\"url\":\"http://localhost/\",\"method\":\"GET\",\"contentType\":\"application/json\",\"accept\":\"application/json\",\"bodyTemplate\":\"{}\",\"iterable\":false,\"iterableKey\":null,\"responseKey\":null}";

    responseEntity = new ResponseEntity<>(request, HttpStatus.OK);

    embeddedVariable = new EmbeddedVariable();
    embeddedVariable.setKey(KEY);
    embeddedVariables = Set.of(embeddedVariable);

    httpHeaders = new HttpHeaders();

    setField(delegate, "folioTokenCache", folioTokenCache);
  }

  @Test
  void testExecuteWorks() throws Exception {
    setupExecuteMocking(false);

    when(httpService.exchange(anyString(), any(HttpMethod.class), any(), any())).thenReturn(responseEntity);

    delegate.execute(delegateExecution);

    verify(delegateExecution, never()).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksAsArray() throws Exception {
    embeddedVariable.setType(VariableType.PROCESS);
    embeddedVariable.setAsArray(true);
    httpHeaders.add(TOKEN_HEADER_NAME, UUID);

    setupExecuteMocking(true);

    delegate.execute(delegateExecution);

    verify(delegateExecution).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksAsArrayAndSpin() throws Exception {
    embeddedVariable.setType(VariableType.PROCESS);
    embeddedVariable.setAsArray(true);
    embeddedVariable.setSpin(true);
    httpHeaders.add(TOKEN_HEADER_NAME, UUID);

    setupExecuteMocking(true);

    delegate.execute(delegateExecution);

    verify(delegateExecution).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksWithKeyLocal() throws Exception {
    embeddedVariable.setType(VariableType.LOCAL);
    httpHeaders.add(TOKEN_HEADER_NAME, UUID);

    setupExecuteMocking(true);

    delegate.execute(delegateExecution);

    verify(delegateExecution, never()).setVariable(anyString(), any());
    verify(delegateExecution).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksWithKeyProcess() throws Exception {
    embeddedVariable.setType(VariableType.PROCESS);
    httpHeaders.add(TOKEN_HEADER_NAME, UUID);

    setupExecuteMocking(true);

    delegate.execute(delegateExecution);

    verify(delegateExecution).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksNoKey() throws Exception {
    setField(embeddedVariable, "key", null);

    setupExecuteMocking(false);

    when(httpService.exchange(anyString(), any(HttpMethod.class), any(), any())).thenReturn(responseEntity);

    delegate.execute(delegateExecution);

    verify(delegateExecution, never()).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksWithToken() throws Exception {
    setupExecuteMocking(false);

    when(folioTokenCache.verifyTokens(any())).thenReturn(UUID);
    when(httpService.exchange(anyString(), any(HttpMethod.class), any(), any())).thenReturn(responseEntity);

    delegate.execute(delegateExecution);

    verify(delegateExecution, never()).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testExecuteWorksWithNoType() throws Exception {
    httpHeaders.add(TOKEN_HEADER_NAME, UUID);

    setField(embeddedVariable, "type", null);

    setupExecuteMocking(true);

    delegate.execute(delegateExecution);

    verify(delegateExecution, never()).setVariable(anyString(), any());
    verify(delegateExecution, never()).setVariableLocal(anyString(), any());
  }

  @Test
  void testSetHeaderOutputVariablesWorks() {
    setField(delegate, "headerOutputVariables", null);

    delegate.setHeaderOutputVariables(requestExpression);
    assertEquals(requestExpression, getField(delegate, "headerOutputVariables"));
  }

  @Test
  void testSetRequestWorks() {
    setField(delegate, "request", null);

    delegate.setRequest(requestExpression);
    assertEquals(requestExpression, getField(delegate, "request"));
  }

  @Test
  void testFromTaskWorks() {
    assertEquals(FolioRequestTask.class, delegate.fromTask());
  }

  /**
   * Provide common mocking behavior for the execute() method.
   *
   * @param hasKey Set to TRUE if key is present; FALSE otherwise.
   *
   * @throws JsonProcessingException
   */
  private void setupExecuteMocking(boolean hasKey) throws JsonProcessingException {
    setField(delegate, "headerOutputVariables", headerOutputVariablesExpression);
    setField(delegate, "httpService", httpService);
    setField(delegate, "request", requestExpression);
    setField(delegate, "objectMapper", mapper);

    if (hasKey) {
      embeddedVariable.setKey(TOKEN_HEADER_NAME);
      setField(responseEntity, "headers", httpHeaders);

      when(httpService.exchange(anyString(), any(HttpMethod.class), any(), any())).thenReturn(responseEntity);
    }

    when(delegateExecution.getBpmnModelElementInstance()).thenReturn(flowElementBpmn);
    when(flowElementBpmn.getName()).thenReturn(KEY);
    when(headerOutputVariablesExpression.getValue(any(DelegateExecution.class))).thenReturn(mapper.writeValueAsString(embeddedVariables));
    when(requestExpression.getValue(any(DelegateExecution.class))).thenReturn(requestStr);
  }

}
