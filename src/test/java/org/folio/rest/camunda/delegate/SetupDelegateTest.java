package org.folio.rest.camunda.delegate;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.LITERAL;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.SECURE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.URL;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.URL_PATH;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_ARRAY;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.NULL_STR;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.config.FolioEnvConfig;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class SetupDelegateTest {

  private static final String PRC1 = "[ { \"id\": \"84d0181e-8e0f-4d80-a580-03fe45b3c179\", \"name\": \"Start\", \"description\": \"Start of Example Javascript ScriptTask Workflow.\", \"type\": \"MESSAGE_CORRELATION\", \"deserializeAs\": \"StartEvent\", \"expression\": \"/events/example-scripttask-js/start\" } ]";

  private static final String SECURE_NAME = "secure_name";
  private static final String SECURE_OTHER = "secure_other";
  private static final String URL_VALUE = "http://localhost";
  private static final String URL_PATH_VALUE = "/location";

  @Spy
  protected ObjectMapper objectMapper;

  @Spy
  protected RuntimeService runtimeService;

  @Mock
  ScriptEngineService scriptEngineService;

  @Mock
  Expression initialContext;

  @Mock
  Expression processors;

  @Mock
  DelegateExecution execution;

  @Mock
  FlowElement element;

  FolioEnvConfig folioEnvConfig;

  List<FolioEnvDefaultsItem> defaults;

  @InjectMocks
  SetupDelegate delegate;

  @BeforeEach
  void beforeEach() {
    folioEnvConfig = new FolioEnvConfig();

    defaults = new ArrayList<>();
    setField(folioEnvConfig, "defaults", defaults);

    delegate.setInitialContext(initialContext);
    delegate.setProcessors(processors);
  }

  @Disabled // Works upstream, disabled because we plan to move and fixing this test is not worth the time and effort in the back port.
  @ParameterizedTest
  @MethodSource("executionStream")
  @SuppressWarnings("unchecked")
  void executeWorksTest(String initialContextValue, String processorsValue, Class<Exception> exception) throws Exception {

    lenient().when(execution.getTenantId()).thenReturn("diku");
    lenient().when(execution.getBpmnModelElementInstance()).thenReturn(element);
    lenient().when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
    lenient().when(initialContext.getValue(any(DelegateExecution.class))).thenReturn(initialContextValue);
    lenient().when(processors.getValue(any(DelegateExecution.class))).thenReturn(processorsValue);

    setField(delegate, "folioEnvConfig", folioEnvConfig);
    setField(delegate, "scriptEngineService", scriptEngineService);

    lenient().doNothing().when(scriptEngineService).registerScript(anyString(), anyString(), anyString());

    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> delegate.execute(execution));
    } else {

      delegate.execute(execution);

      verify(element, times(2)).getName();
      verify(initialContext, times(1)).getValue(any(DelegateExecution.class));
      verify(processors, times(1)).getValue(any(DelegateExecution.class));
      verify(objectMapper, times(1)).readValue(eq(initialContextValue), any(TypeReference.class));

      // initialContext are not yet used and are subject to removal
      // for each initial context variable
      // verify objectMapper writeValueAsString and execution setVariable for each initial context

      verify(execution, times(1)).setVariable(eq("timestamp"), anyString());
      verify(execution, times(1)).setVariable("tenantId", "diku");

      // processors are not yet used and are subject to removal
      // for each processor
      // mock processor getScriptType getExtension chain
      // mock processor getFunctionName and getCode
      // mock scriptEngineService registerScript
      // verify objectMapper writeValueAsString and execution setVariable for each initial context
    }
  }

  @Disabled // Works upstream, disabled because we plan to move and fixing this test is not worth the time and effort in the back port.
  @Test
  @SuppressWarnings("unchecked")
  void loadEnvConfigItemWorksTest() throws Exception {

    when(execution.getTenantId()).thenReturn("diku");
    when(execution.getBpmnModelElementInstance()).thenReturn(element);
    when(execution.hasVariable(VALUE)).thenReturn(false);
    when(execution.hasVariable(SECURE_NAME)).thenReturn(true);
    when(execution.hasVariable(SECURE_OTHER)).thenReturn(false);
    when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
    when(initialContext.getValue(any(DelegateExecution.class))).thenReturn(JSON_OBJECT);
    when(processors.getValue(any(DelegateExecution.class))).thenReturn(PRC1);

    setField(delegate, "folioEnvConfig", folioEnvConfig);
    setField(delegate, "scriptEngineService", scriptEngineService);

    defaults.add(new FolioEnvDefaultsItem(true, VALUE, LITERAL, UUID));
    defaults.add(new FolioEnvDefaultsItem(true, VALUE, SECURE, UUID));
    defaults.add(new FolioEnvDefaultsItem(true, SECURE_NAME, SECURE, UUID));
    defaults.add(new FolioEnvDefaultsItem(true, VALUE, URL, URL_VALUE));
    defaults.add(new FolioEnvDefaultsItem(true, VALUE, URL_PATH, URL_PATH_VALUE));
    defaults.add(new FolioEnvDefaultsItem(false, SECURE_NAME, SECURE, UUID));
    defaults.add(new FolioEnvDefaultsItem(false, SECURE_OTHER, SECURE, UUID));

    lenient().doNothing().when(scriptEngineService).registerScript(anyString(), anyString(), anyString());

    delegate.execute(execution);

    verify(element, times(1)).getName();
    verify(initialContext, times(1)).getValue(any(DelegateExecution.class));
    verify(processors, times(1)).getValue(any(DelegateExecution.class));
    verify(objectMapper, times(1)).readValue(eq(JSON_OBJECT), any(TypeReference.class));

    verify(execution, times(1)).setVariable(eq("timestamp"), anyString());
    verify(execution, times(1)).setVariable("tenantId", "diku");
  }

  @Disabled // Works upstream, disabled because we plan to move and fixing this test is not worth the time and effort in the back port.
  @Test
  @SuppressWarnings("unchecked")
  void loadEnvConfigItemNoneWorksTest() throws Exception {

    when(execution.getTenantId()).thenReturn("diku");
    when(execution.getBpmnModelElementInstance()).thenReturn(element);
    when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
    when(initialContext.getValue(any(DelegateExecution.class))).thenReturn(JSON_OBJECT);
    when(processors.getValue(any(DelegateExecution.class))).thenReturn(PRC1);

    setField(delegate, "folioEnvConfig", folioEnvConfig);
    setField(delegate, "scriptEngineService", scriptEngineService);
    setField(folioEnvConfig, "defaults", null);

    lenient().doNothing().when(scriptEngineService).registerScript(anyString(), anyString(), anyString());

    delegate.execute(execution);

    verify(element, times(1)).getName();
    verify(initialContext, times(1)).getValue(any(DelegateExecution.class));
    verify(processors, times(1)).getValue(any(DelegateExecution.class));
    verify(objectMapper, times(1)).readValue(eq(JSON_OBJECT), any(TypeReference.class));

    verify(execution, times(1)).setVariable(eq("timestamp"), anyString());
    verify(execution, times(1)).setVariable("tenantId", "diku");
  }

  @Disabled // Works upstream, disabled because we plan to move and fixing this test is not worth the time and effort in the back port.
  @Test
  void loadEnvConfigItemThrowsTest() throws Exception {

    when(execution.getTenantId()).thenReturn("diku");
    when(execution.getBpmnModelElementInstance()).thenReturn(element);
    when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
    when(initialContext.getValue(any(DelegateExecution.class))).thenReturn(JSON_OBJECT);
    when(processors.getValue(any(DelegateExecution.class))).thenReturn(PRC1);

    setField(delegate, "folioEnvConfig", folioEnvConfig);
    setField(delegate, "scriptEngineService", scriptEngineService);

    defaults.add(new FolioEnvDefaultsItem(true, VALUE, LITERAL, UUID));

    doThrow(new RuntimeException()).when(scriptEngineService).registerScript(anyString(), anyString(), anyString());

    assertThrows(Exception.class, () -> {
      delegate.execute(execution);
    });
  }

  /**
   * Helper function for parameterized test providing tests with
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - initial context variables (JSON map of type Map<String, Object>)
   *     - processors to register (JSON list of type EmbeddedProcessor)
   *     - exception that is expected to be thrown for inputs
   */
  private static Stream<Arguments> executionStream() {

    final String ctx1 = "{ \"a\": 0 }";
    final String ctx2 = "{ \"a\": 0, \"b\": \"bee\" }";
    final String empty = "";
    final Class<?> failure = Exception.class;

    return Stream.of(
      Arguments.of(NULL_STR,    NULL_STR,   failure),
      Arguments.of(NULL_STR,    empty,      failure),
      Arguments.of(NULL_STR,    JSON_ARRAY, failure),
      Arguments.of(empty,       NULL_STR,   failure),
      Arguments.of(empty,       empty,      failure),
      Arguments.of(empty,       JSON_ARRAY, failure),
      Arguments.of(JSON_OBJECT, NULL_STR,   failure),
      Arguments.of(JSON_OBJECT, empty,      failure),
      Arguments.of(JSON_OBJECT, JSON_ARRAY, null),
      Arguments.of(ctx1,        JSON_ARRAY, null),
      Arguments.of(ctx2,        JSON_ARRAY, null),
      Arguments.of(JSON_OBJECT, PRC1,       null)
    );
  }


}
