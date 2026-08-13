package org.folio.rest.camunda.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FolioEnvDefaultsItemTypeTest {

  @ParameterizedTest
  @MethodSource("provideEnumValues")
  void enumerationIsValidTest(FolioEnvDefaultsItemType enumeration, String expect) {
    assertEquals(expect, enumeration.name());
  }

  @ParameterizedTest
  @MethodSource("provideGetNameValues")
  void enumerationGetNameTest(FolioEnvDefaultsItemType enumeration, String expect) {
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
      Arguments.of(FolioEnvDefaultsItemType.LITERAL,  "LITERAL"),
      Arguments.of(FolioEnvDefaultsItemType.SECURE,   "SECURE"),
      Arguments.of(FolioEnvDefaultsItemType.URL,      "URL"),
      Arguments.of(FolioEnvDefaultsItemType.URL_PATH, "URL_PATH")
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
      Arguments.of(FolioEnvDefaultsItemType.LITERAL,  "literal"),
      Arguments.of(FolioEnvDefaultsItemType.SECURE,   "secure"),
      Arguments.of(FolioEnvDefaultsItemType.URL,      "url"),
      Arguments.of(FolioEnvDefaultsItemType.URL_PATH, "url_path")
    );
  }

}
