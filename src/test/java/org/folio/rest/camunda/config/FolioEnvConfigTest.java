package org.folio.rest.camunda.config;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.LITERAL;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.SECURE;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
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
  @MethodSource("provideChangeItemWorksValues")
  void changeItemWorksTest(ConcurrentMap<String, FolioEnvDefaultsItem> map, FolioEnvDefaultsItem item, boolean expect) {

    setField(folioEnvConfig, DEFAULTS, map);

    final boolean result = folioEnvConfig.changeItem(item);

    assertEquals(expect, result);
  }

  @ParameterizedTest
  @MethodSource("provideChangeItemValueWorksValues")
  void changeItemValueWorksTest(ConcurrentMap<String, FolioEnvDefaultsItem> map, FolioEnvDefaultsItem item, boolean expect) {

    setField(folioEnvConfig, DEFAULTS, map);

    final boolean result = folioEnvConfig.changeItemValue(item.getName(), UUID);

    assertEquals(expect, result);
  }

  @ParameterizedTest
  @MethodSource("provideEachItemWorksValues")
  void eachItemWorksTest(ConcurrentMap<String, FolioEnvDefaultsItem> map, AtomicInteger count, int times) {

    setField(folioEnvConfig, DEFAULTS, map);

    folioEnvConfig.eachItem((key, item) -> {
      count.incrementAndGet();
    });

    assertEquals(times, count.get());
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
  @MethodSource("provideHasDefaultsWorksValues")
  void hasDefaultsWorksTest(ConcurrentMap<String, FolioEnvDefaultsItem> map, boolean expect) {

    setField(folioEnvConfig, DEFAULTS, map);

    final boolean result = folioEnvConfig.hasDefaults();

    assertEquals(expect, result);
  }

  @ParameterizedTest
  @MethodSource("provideInitializeItemWorksValues")
  void initializeItemWorksTest(String field, ConcurrentMap<String, FolioEnvDefaultsItem> map, FolioEnvDefaultsItem item, boolean expect) {

    setField(folioEnvConfig, DEFAULTS, map);

    final boolean result = folioEnvConfig.initializeItem(item);

    assertEquals(expect, result);

    if (map != null) {
      assertEquals(expect == true ? 1 : 0, map.size());
    }
  }

  @ParameterizedTest
  @MethodSource("provideRetrieveItemWorksValues")
  void retrieveItemWorksTest(ConcurrentMap<String, FolioEnvDefaultsItem> map, String name, FolioEnvDefaultsItem expect) {

    setField(folioEnvConfig, DEFAULTS, map);

    final FolioEnvDefaultsItem result = folioEnvConfig.retrieveItem(name);

    assertEquals(expect, result);
  }

  @ParameterizedTest
  @MethodSource("provideRetrieveItemValueWorksValues")
  void retrieveItemValueWorksTest(ConcurrentMap<String, FolioEnvDefaultsItem> map, String name, String expect) {

    setField(folioEnvConfig, DEFAULTS, map);

    final String result = folioEnvConfig.retrieveItemValue(name);

    assertEquals(expect, result);
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
   * Provide variables for performing change item value tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:.
   *     - map:     The map value.
   *     - item:    The spied item.
   *     - expect:  TRUE if method is expected to called and FALSE otherwise.
   */
  private static Stream<Arguments> provideChangeItemWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapEmpty = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapFill = new ConcurrentHashMap<>();

    final FolioEnvDefaultsItem itemNoName = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemSecure = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemValue = new FolioEnvDefaultsItem();

    mapFill.put(LITERAL.getName(), itemValue);

    setField(itemSecure, "name", SECURE.getName());
    setField(itemSecure, "type", SECURE);
    setField(itemSecure, "value", DEFAULTS);

    setField(itemValue, "name", LITERAL.getName());
    setField(itemValue, "type", LITERAL);
    setField(itemValue, "value", DEFAULTS);

    arguments.add(Arguments.of(null,     itemValue,  false));
    arguments.add(Arguments.of(mapEmpty, null,       false));
    arguments.add(Arguments.of(mapEmpty, itemNoName, false));
    arguments.add(Arguments.of(mapEmpty, itemValue,  false));
    arguments.add(Arguments.of(mapFill,  itemNoName, false));
    arguments.add(Arguments.of(mapFill,  itemSecure, false));
    arguments.add(Arguments.of(mapFill,  itemValue,  true));

    return arguments.stream();
  }

  /**
   * Provide variables for performing change item value tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:.
   *     - map:     The map value.
   *     - item:    The spied item.
   *     - expect:  TRUE if method is expected to called and FALSE otherwise.
   */
  private static Stream<Arguments> provideChangeItemValueWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapEmpty = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapFill = new ConcurrentHashMap<>();

    final FolioEnvDefaultsItem itemSecure = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemValue = new FolioEnvDefaultsItem();

    mapFill.put(SECURE.getName(), itemSecure);
    mapFill.put(LITERAL.getName(), itemValue);

    setField(itemSecure, "name", SECURE.getName());
    setField(itemSecure, "type", SECURE);
    setField(itemSecure, "value", DEFAULTS);

    setField(itemValue, "name", LITERAL.getName());
    setField(itemValue, "type", LITERAL);
    setField(itemValue, "value", DEFAULTS);

    arguments.add(Arguments.of(null,     itemSecure, false));
    arguments.add(Arguments.of(null,     itemValue,  false));
    arguments.add(Arguments.of(mapEmpty, itemSecure, false));
    arguments.add(Arguments.of(mapEmpty, itemValue,  false));
    arguments.add(Arguments.of(mapFill,  itemSecure, true));
    arguments.add(Arguments.of(mapFill,  itemValue,  true));

    return arguments.stream();
  }

  /**
   * Provide variables for performing (for) each item tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:.
   *     - map:    The map value.
   *     - count:  An atomic integer to count the calls.
   *     - times:  The expected number of calls.
   */
  private static Stream<Arguments> provideEachItemWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapEmpty = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapOne = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapTwo = new ConcurrentHashMap<>();
    final FolioEnvDefaultsItem itemOne = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemTwo = new FolioEnvDefaultsItem();

    final String noName = "noName";
    final String named  = "named";

    mapOne.put(noName, itemOne);

    mapTwo.put(noName, itemOne);
    mapTwo.put(named, itemTwo);

    // For miss.
    arguments.add(Arguments.of(null,     new AtomicInteger(0), 0));
    arguments.add(Arguments.of(mapEmpty, new AtomicInteger(0), 0));

    // For hit.
    arguments.add(Arguments.of(mapOne, new AtomicInteger(0), 1));
    arguments.add(Arguments.of(mapTwo, new AtomicInteger(0), 2));

    return arguments.stream();
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
    final ConcurrentMap<String, FolioEnvDefaultsItem> defaults = new ConcurrentHashMap<>();

    arguments.add(Arguments.of(DEFAULTS, defaults, defaults, "getDefaults"));

    return arguments.stream();
  }

  /**
   * Provide variables for performing has defaults tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:.
   *     - map:     The map value.
   *     - expect:  The expected return result.
   */
  private static Stream<Arguments> provideHasDefaultsWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> map = new ConcurrentHashMap<>();

    arguments.add(Arguments.of(null, false));
    arguments.add(Arguments.of(map,  true));

    return arguments.stream();
  }

  /**
   * Provide variables for performing initialize item tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - field:   The field name.
   *     - map:     The map value.
   *     - item:    The item to test.
   *     - expect:  The expected return result.
   */
  private static Stream<Arguments> provideInitializeItemWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> map = new ConcurrentHashMap<>();
    final FolioEnvDefaultsItem itemNoName = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemNamed = new FolioEnvDefaultsItem();

    setField(itemNamed, "name", DEFAULTS);

    arguments.add(Arguments.of(DEFAULTS, null, null,       false));
    arguments.add(Arguments.of(DEFAULTS, map,  null,       false));
    arguments.add(Arguments.of(DEFAULTS, map,  itemNoName, false));
    arguments.add(Arguments.of(DEFAULTS, map,  itemNamed,  true));

    return arguments.stream();
  }

  /**
   * Provide variables for performing retrieve item tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:.
   *     - map:     The map value.
   *     - name:    The name to use.
   *     - expect:  The expected return result.
   */
  private static Stream<Arguments> provideRetrieveItemWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapEmpty = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapItem = new ConcurrentHashMap<>();
    final FolioEnvDefaultsItem itemNoName = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemNamed = new FolioEnvDefaultsItem();

    final String noName = "noName";
    final String named  = "named";

    mapItem.put(noName, itemNoName);
    mapItem.put(named, itemNamed);

    setField(itemNamed, "name", named);

    // For miss.
    arguments.add(Arguments.of(null,     DEFAULTS, null));
    arguments.add(Arguments.of(null,     named,    null));
    arguments.add(Arguments.of(mapEmpty, DEFAULTS, null));
    arguments.add(Arguments.of(mapItem,  DEFAULTS, null));
    arguments.add(Arguments.of(mapItem,  DEFAULTS, null));

    // For hit.
    arguments.add(Arguments.of(mapItem, noName, itemNoName));
    arguments.add(Arguments.of(mapItem, noName, itemNoName));
    arguments.add(Arguments.of(mapItem, named,  itemNamed));
    arguments.add(Arguments.of(mapItem, named,  itemNamed));

    return arguments.stream();
  }

  /**
   * Provide variables for performing retrieve item value tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:.
   *     - map:     The map value.
   *     - name:    The name to use.
   *     - expect:  The expected return result.
   */
  private static Stream<Arguments> provideRetrieveItemValueWorksValues() {

    final List<Arguments> arguments = new ArrayList<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapEmpty = new ConcurrentHashMap<>();
    final ConcurrentMap<String, FolioEnvDefaultsItem> mapItem = new ConcurrentHashMap<>();
    final FolioEnvDefaultsItem itemNoName = new FolioEnvDefaultsItem();
    final FolioEnvDefaultsItem itemNamed = new FolioEnvDefaultsItem();

    final String noName = "noName";
    final String named  = "named";

    mapItem.put(noName, itemNoName);
    mapItem.put(named, itemNamed);

    setField(itemNamed, "name", named);
    setField(itemNamed, "value", UUID);

    // For miss.
    arguments.add(Arguments.of(null,     DEFAULTS, null));
    arguments.add(Arguments.of(null,     named,    null));
    arguments.add(Arguments.of(mapEmpty, DEFAULTS, null));
    arguments.add(Arguments.of(mapItem,  DEFAULTS, null));
    arguments.add(Arguments.of(mapItem,  DEFAULTS, null));

    // For hit.
    arguments.add(Arguments.of(mapItem, noName, null));
    arguments.add(Arguments.of(mapItem, noName, null));
    arguments.add(Arguments.of(mapItem, named,  UUID));
    arguments.add(Arguments.of(mapItem, named,  UUID));

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
    final ConcurrentMap<String, FolioEnvDefaultsItem> map = new ConcurrentHashMap<>();

    arguments.add(Arguments.of(DEFAULTS, ConcurrentMap.class, map, "setDefaults"));

    return arguments.stream();
  }

}
