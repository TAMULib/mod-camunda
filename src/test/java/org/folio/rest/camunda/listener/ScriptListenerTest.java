package org.folio.rest.camunda.listener;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.LITERAL;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.camunda.config.FolioEnvConfig;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

@ExtendWith(MockitoExtension.class)
class ScriptListenerTest {

  @Mock
  DelegateExecution execution;

  private FolioEnvConfig folioEnvConfig;

  private ScriptListener listener;

  @BeforeEach
  void beforeEach() {

    folioEnvConfig = new FolioEnvConfig();
    listener = spy(new ScriptListener(folioEnvConfig));
  }

  @ParameterizedTest
  @MethodSource("provideNotifyEnvDefaultsValues")
  void notifyEnvDefaultsTest(final ConcurrentMap<String, FolioEnvDefaultsItem> defaults, final VerificationMode times, final Scope scope) throws Exception {

    setField(folioEnvConfig, "defaults", defaults);

    if (Scope.PROCESS.equals(scope) || Scope.BOTH.equals(scope)) {
      when(execution.hasVariable(anyString())).thenReturn(true);
    }

    if (Scope.LOCAL.equals(scope) || Scope.BOTH.equals(scope)) {
      when(execution.hasVariableLocal(anyString())).thenReturn(true);
    }

    listener.notify(execution);

    verify(listener, times).processEnvConfig(any(), any());

    if (Scope.BOTH.equals(scope)) {
      final int total = defaults.size();

      verify(execution, times(total)).hasVariable(any());
      verify(execution, times(total)).removeVariable(any());
    }
  }

  /**
   * Provide variables for when notify() is called when there are items to loop over.
   *
   * The scope other than NULL is simple and doesn't support multiple items wben determining variable validation. 
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - defaults: The defaults to set.
   *     - times:    The number of times to be verified.
   *     - scope:    The Scope value for defining variables found.
   */
  private static Stream<Arguments> provideNotifyEnvDefaultsValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapNull = null;
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapEmpty = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> map1 = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> map2 = new ConcurrentHashMap<>();

    final FolioEnvDefaultsItem item1 = new FolioEnvDefaultsItem(false, VALUE, LITERAL, UUID);
    final FolioEnvDefaultsItem item2 = new FolioEnvDefaultsItem(true, VALUE, LITERAL, UUID);

    map1.compute("item1", (key, existing) -> item1);
    map2.compute("item1", (key, existing) -> item1);
    map2.compute("item2", (key, existing) -> item2);

    arguments.add(Arguments.of(mapNull,  never(),  Scope.NONE));
    arguments.add(Arguments.of(mapEmpty, never(),  Scope.NONE));
    arguments.add(Arguments.of(map1,     times(1), Scope.NONE));
    arguments.add(Arguments.of(map2,     times(2), Scope.NONE));
    arguments.add(Arguments.of(map1,     times(1), Scope.PROCESS));
    arguments.add(Arguments.of(map1,     times(1), Scope.LOCAL));
    arguments.add(Arguments.of(map1,     times(1), Scope.BOTH));

    return arguments.stream();
  }

  /**
   * Define the scope for use in the unit tests.
   */
  private static enum Scope {
    NONE,
    PROCESS,
    LOCAL,
    BOTH,
  }

}
