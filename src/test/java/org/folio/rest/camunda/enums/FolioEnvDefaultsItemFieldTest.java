package org.folio.rest.camunda.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FolioEnvDefaultsItemFieldTest {

  @ParameterizedTest
  @MethodSource("provideEnumValues")
  void enumerationIsValidTest(FolioEnvDefaultsItemField enumeration, String expect) {
    assertEquals(expect, enumeration.name());
  }

  @ParameterizedTest
  @MethodSource("provideGetNameValues")
  void enumerationGetNameTest(FolioEnvDefaultsItemField enumeration, String expect) {
    assertEquals(expect, enumeration.getName());
  }

  /**
   * Provide variables for the given enumeration parameterized test.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - enumeration: The enumeration value.
   *     - expect:      The string that the enumeration is expected to match.
   */
  private static Stream<Arguments> provideEnumValues() {
    return Stream.of(
      Arguments.of(FolioEnvDefaultsItemField.EXPOSE, "EXPOSE"),
      Arguments.of(FolioEnvDefaultsItemField.NAME,   "NAME"),
      Arguments.of(FolioEnvDefaultsItemField.TYPE,   "TYPE"),
      Arguments.of(FolioEnvDefaultsItemField.VALUE,  "VALUE")
    );
  }

  /**
   * Provide variables for the given enumeration parameterized test.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - enumeration: The enumeration value.
   *     - expect:      The string that the getName() is expected to return.
   */
  private static Stream<Arguments> provideGetNameValues() {
    return Stream.of(
      Arguments.of(FolioEnvDefaultsItemField.EXPOSE, "expose"),
      Arguments.of(FolioEnvDefaultsItemField.NAME,   "name"),
      Arguments.of(FolioEnvDefaultsItemField.TYPE,   "type"),
      Arguments.of(FolioEnvDefaultsItemField.VALUE,  "value")
    );
  }

}
