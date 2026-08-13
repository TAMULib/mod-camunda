package org.folio.rest.camunda.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FolioEnvConfigTest {

  private static final String DEFAULTS = "defaults";

  @InjectMocks
  private FolioEnvConfig folioEnvConfig;

  @BeforeEach
  void beforeEach() {

    folioEnvConfig = new FolioEnvConfig();
  }

  @ParameterizedTest
  @MethodSource("provideGettersWorksValues")
  void gettersWorkTest(String field, Object initial, Object expect, String name)
    throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

    final Method method = FolioEnvConfig.class.getDeclaredMethod(name);

    setField(folioEnvConfig, field, initial);

    assertEquals(expect, method.invoke(folioEnvConfig));
  }

  @ParameterizedTest
  @MethodSource("provideSettersWorksValues")
  void settersWorkTest(String field, Class<?> type, Object expect, String name)
    throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

    final Method method = FolioEnvConfig.class.getDeclaredMethod(name, type);

    setField(folioEnvConfig, field, null);

    method.invoke(folioEnvConfig, expect);

    assertEquals(expect, getField(folioEnvConfig, field));
  }

  /**
   * Provide variables for performing initialization tests for getters.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - field:   The field name.
   *     - initial: The initial value.
   *     - expect:  The expected value.
   *     - name:    The method name.
   */
  private static Stream<Arguments> provideGettersWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final List<FolioEnvDefaultsItem> defaults = new ArrayList<>();

    arguments.add(Arguments.of(DEFAULTS, defaults, defaults, "getDefaults"));

    return arguments.stream();
  }

  /**
   * Provide variables for performing initialization tests for setters.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - field:  The field name.
   *     - type:   The class type.
   *     - expect: The value to assign and also the expected value.
   *     - name:   The method name.
   */
  private static Stream<Arguments> provideSettersWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final List<FolioEnvDefaultsItem> defaults = new ArrayList<>();

    arguments.add(Arguments.of(DEFAULTS, List.class, defaults, "setDefaults"));

    return arguments.stream();
  }

}
