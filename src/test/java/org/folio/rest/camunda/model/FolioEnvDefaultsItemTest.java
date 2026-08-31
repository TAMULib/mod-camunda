package org.folio.rest.camunda.model;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.EXPOSE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.NAME;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.TYPE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.VALUE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.LITERAL;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.SECURE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.URL;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.URL_PATH;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.folio.rest.camunda.exception.FolioEnvDefaultsItemFieldException;
import org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType;
import org.folio.rest.camunda.record.FolioTokensRecord;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.GuardedString.Accessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FolioEnvDefaultsItemTest {

  private static final String GUARD = "guard";
  private static final String PATH_EXTRA = "///valid///";
  private static final String PATH_VALID = "/valid";
  private static final String PATH_INVALID = "@invalid::";
  private static final String URL_VALID   = "http://localhost";
  private static final String URL_INVALID = "http://localhost::////";
  private static final String VALID = "valid";

  @Mock
  private GuardedString guard;
  
  @Mock
  private Accessor accessor;

  private FolioEnvDefaultsItem item;

  @BeforeEach
  void beforeEach() {

    item = new FolioEnvDefaultsItem();

    setField(item, GUARD, guard);
  }

  @Test
  void getSecureCallsAccessorTest() {

    setField(item, TYPE.getName(), SECURE);

    final boolean result = item.getSecure(accessor);

    assertTrue(result);
    verify(guard).access(accessor);
  }

  @Test
  void getSecureReturnsFalseTest() {

    setField(item, TYPE.getName(), LITERAL);

    final boolean result = item.getSecure(accessor);

    assertFalse(result);
    verify(guard, never()).access(accessor);
  }

  @Test
  void getSecureHandlesNullTest() {

    setField(item, TYPE.getName(), SECURE);
    setField(item, GUARD, null);

    final boolean result = item.getSecure(accessor);

    assertFalse(result);
    verify(guard, never()).access(accessor);
  }

  @ParameterizedTest
  @MethodSource("provideGettersWorksValues")
  void gettersWorkTest(String field, Object initial, Object expect, String name)
    throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

    final Method method = FolioEnvDefaultsItem.class.getDeclaredMethod(name);

    setField(item, field, initial);

    assertEquals(expect, method.invoke(item));
  }

  @ParameterizedTest
  @MethodSource("provideInitializeBooleanValues")
  void initializeDefaultsExposeTest(Boolean givenExpose, Boolean expectExpose) {

    item = new FolioEnvDefaultsItem(givenExpose, VALID, LITERAL, UUID);

    assertEquals(expectExpose, getField(item, EXPOSE.getName()));
  }

  @ParameterizedTest
  @MethodSource("provideInitializeThrowsExceptionValues")
  void initializeFieldNameThrowsOnNullTest(Boolean expose, String name, FolioEnvDefaultsItemType type, String value, String has, String lacks) {

    final RuntimeException e = assertThrows(FolioEnvDefaultsItemFieldException.class, () -> {
      new FolioEnvDefaultsItem(expose, name, type, value);
    });

    final String message = e.getMessage();

    assertNotNull(message);
    if (has != null) assertTrue(e.getMessage().contains(has));
    if (lacks != null) assertFalse(e.getMessage().contains(lacks));
  }

  @ParameterizedTest
  @MethodSource("provideInitializeTypeValueNullValues")
  void initializeTypeValueNullTest(FolioEnvDefaultsItemType type) {

    item = new FolioEnvDefaultsItem(true, VALID, type, null);

    assertNotNull(item);
    assertNull(getField(item, VALUE.getName()));
  }

  @ParameterizedTest
  @MethodSource("provideInitializeTypeUrlValues")
  void initializeTypeUrlPathValidWithExtraTest(Boolean expose, String value, String expect) {

    item = new FolioEnvDefaultsItem(expose, VALID, URL_PATH, value);

    assertNotNull(item);
    assertEquals(expect, getField(item, VALUE.getName()));
  }

  @Test
  void initializeTypeUrlValidTest() {

    item = new FolioEnvDefaultsItem(true, VALID, URL, URL_VALID);

    assertNotNull(item);
    assertEquals(URL_VALID, getField(item, VALUE.getName()));
  }

  @Test
  void preDestroyWorksTest() {

    item.preDestroy();

    verify(guard).dispose();
  }

  @Test
  void prepareByTypeForSecureCallsSetSecureTest() {

    item = spy(new FolioEnvDefaultsItem());

    setField(item, NAME.getName(), VALID);
    setField(item, TYPE.getName(), SECURE);

    item.prepareByType(null);

    assertNull(getField(item, VALUE.getName()));
    verify(item).setSecure(any());
  }

  @Test
  void prepareByTypeForSecureCreatesGuardTest() {

    item = spy(new FolioEnvDefaultsItem());

    setField(item, NAME.getName(), VALID);
    setField(item, TYPE.getName(), SECURE);

    item.prepareByType(UUID);

    assertNotNull(getField(item, GUARD));
    verify(item).setSecure(UUID);
  }

  @ParameterizedTest
  @MethodSource("provideSettersWorksValues")
  void settersWorkTest(String field, Class<?> type, Object expect, String name)
    throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

    final Method method = FolioEnvDefaultsItem.class.getDeclaredMethod(name, type);

    setField(item, field, null);

    method.invoke(item, expect);

    assertEquals(expect, getField(item, field));
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

    arguments.add(Arguments.of(EXPOSE.getName(), true,    true,    "getExpose"));
    arguments.add(Arguments.of(NAME.getName(),   VALID,   VALID,   "getName"));
    arguments.add(Arguments.of(TYPE.getName(),   LITERAL, LITERAL, "getType"));
    arguments.add(Arguments.of(VALUE.getName(),  VALID,   VALID,   "getValue"));

    return arguments.stream();
  }

  /**
   * Provide variables for performing initialization tests for boolean fields.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - givenExpose:  The value to initialize expose as.
   *     - expectExpose: The expected value to be assigned to expose.
   */
  private static Stream<Arguments> provideInitializeBooleanValues() {

    final List<Arguments> arguments = new ArrayList<>();

    // Test for expose.
    arguments.add(Arguments.of(null,  true));
    arguments.add(Arguments.of(false, false));

    return arguments.stream();
  }

  /**
   * Provide variables for performing initialization tests throwing exceptions.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - expose: The expose value.
   *     - name:   The name value.
   *     - type:   The type value.
   *     - value:  The value value.
   *     - has:    Message contains this string, if not NULL.
   *     - lacks:  Message does not contain this string, if not NULL.
   */
  private static Stream<Arguments> provideInitializeThrowsExceptionValues() {

    final List<Arguments> arguments = new ArrayList<>();

    final String fieldName = "field '" + NAME.getName() + "'";
    final String fieldType = "field '" + TYPE.getName() + "'";
    final String fieldValue = "field '" + VALUE.getName() + "'";
    final String badFormat = "may only contain word characters without leading digits";

    arguments.add(Arguments.of(null, null,  null,     null,         fieldName,  null));
    arguments.add(Arguments.of(null, VALID, null,     null,         fieldType,  fieldName));
    arguments.add(Arguments.of(true, VALID, URL_PATH, PATH_INVALID, fieldValue, null));
    arguments.add(Arguments.of(true, VALID, URL,      URL_INVALID,  fieldValue, null));
    arguments.add(Arguments.of(true, VALID, URL,      UUID,         fieldValue, null));
    arguments.add(Arguments.of(true, UUID,  URL,      UUID,         badFormat,  null));

    return arguments.stream();
  }

  /**
   * Provide variables for performing initialization tests for the value variable.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - expose: The expose value.
   *     - value:  The value.
   *     - expect: The expected value.
   */
  private static Stream<Arguments> provideInitializeTypeUrlValues() {

    final List<Arguments> arguments = new ArrayList<>();

    arguments.add(Arguments.of(true,  PATH_VALID, PATH_VALID));
    arguments.add(Arguments.of(true,  PATH_EXTRA, PATH_VALID));
    arguments.add(Arguments.of(false, PATH_VALID, PATH_VALID));
    arguments.add(Arguments.of(false, PATH_EXTRA, PATH_VALID));

    return arguments.stream();
  }

  /**
   * Provide variables for performing initialization tests for boolean fields.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - type: The type.
   */
  private static Stream<Arguments> provideInitializeTypeValueNullValues() {

    final List<Arguments> arguments = new ArrayList<>();

    arguments.add(Arguments.of(URL));
    arguments.add(Arguments.of(URL_PATH));

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

    final Class<?> asBool = Boolean.class;
    final Class<?> asString = String.class;
    final Class<?> asType = FolioEnvDefaultsItemType.class;

    arguments.add(Arguments.of(EXPOSE.getName(), asBool,   true,    "setExpose"));
    arguments.add(Arguments.of(NAME.getName(),   asString, VALID,   "setName"));
    arguments.add(Arguments.of(TYPE.getName(),   asType,   LITERAL, "setType"));
    arguments.add(Arguments.of(VALUE.getName(),  asString, UUID,    "setValue"));

    return arguments.stream();
  }

}
