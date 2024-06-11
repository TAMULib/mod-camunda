package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FormatUtilityTest {

  static class TestData {
    String input;
    String expected;

    TestData(String input, String expected) {
      this.input = input;
      this.expected = expected;
    }
  }

  // NOTE: sanitizeSqlCode method is only replaceing single quotes with double quotes
  static Stream<TestData> sqlStream() {
    return Stream.of(new TestData[] {
        new TestData(null, null),
        new TestData("", ""),
        new TestData("'", "''"),
    });
  }

  static Stream<TestData> cqlStream() {
    return Stream.of(new TestData[] {
        new TestData(null, "\"\""),
        new TestData("", "\"\""),
        new TestData("simple", "\"simple\""),
        new TestData("hello*world", "\"hello\\*world\""),
        new TestData("question?", "\"question\\?\""),
        new TestData("caret^symbol", "\"caret\\^symbol\""),
        new TestData("quote\"test", "\"quote\\\"test\""),
        new TestData("ampersand&sign", "\"ampersand%26sign\""),
        new TestData("special&*^?\\", "\"special%26\\*\\^\\?\\\\\""),
        new TestData("text with spaces", "\"text with spaces\""),
        new TestData("*&^%$#@!", "\"\\*%26\\^%$#@!\""),
        new TestData("newline\ntext", "\"newline\ntext\""),
        new TestData("tab\ttext", "\"tab\ttext\""),
        new TestData("混合文本", "\"混合文本\""),
        new TestData("text:\"apple\"", "\"text:\\\"apple\\\"\""),
        new TestData("\"red apple\"", "\"\\\"red apple\\\"\""),
        new TestData("text:\"apple\" AND color:\"red\"", "\"text:\\\"apple\\\" AND color:\\\"red\\\"\""),
        new TestData("text:\"apple\" NEAR/2 \"pie\"", "\"text:\\\"apple\\\" NEAR/2 \\\"pie\\\"\""),
        new TestData("title:\"apple\" AND author:\"John\"", "\"title:\\\"apple\\\" AND author:\\\"John\\\"\""),
        new TestData("price:[10 TO 100]", "\"price:[10 TO 100]\""),
        new TestData("text:\"appl~\"", "\"text:\\\"appl~\\\"\""),
        new TestData("(text:\"apple\" OR text:\"orange\") AND NOT color:\"green\"",
            "\"(text:\\\"apple\\\" OR text:\\\"orange\\\") AND NOT color:\\\"green\\\"\""),
    });
  }

  static Stream<TestData> postalCodeStream() {
    return Stream.of(new TestData[] {
        new TestData("75201", "75201"),
        new TestData("10001-", "10001"),
        new TestData("60601-4321", "60601-4321"),
        new TestData("981013333", "98101-3333"),
        new TestData("", ""),
    });
  }

  static Stream<TestData> phoneNumberStream() {
    return Stream.of(new TestData[] {
        new TestData("+1 650-253-0000", "(650) 253-0000"),
        new TestData("16502530000", "(650) 253-0000"),
        new TestData("650-253-0000", "(650) 253-0000"),
        new TestData("(650) 253-0000", "(650) 253-0000"),
        new TestData("+44 20 7031 3000", "+44 20 7031 3000"),
        new TestData("+91 22 2778 2778", "+91 22 2778 2778"),
        new TestData("442070313000", "442070313000"),
        new TestData("912227782778", "912227782778"),
        new TestData("#12345", "#12345"),
        new TestData("*12345", "*12345"),
        new TestData("#9876*", "#9876*"),
        new TestData("*#7890", "*#7890"),
        new TestData("abcdef", "abcdef"),
        new TestData("123", "123"),
        new TestData("phone number", "phone number"),
        new TestData("", ""),
        new TestData(null, null),
        new TestData("650-253-0000 ext. 123", "(650) 253-0000 ext. 123"),
        new TestData("+1-800-FLOWERS", "(800) 356-9377"),
    });
  }

  @ParameterizedTest
  @MethodSource("sqlStream")
  public void testSanitizeSqlCode(TestData data) {
    assertEquals(data.expected, FormatUtility.sanitizeSqlCode(data.input));
  }

  @ParameterizedTest
  @MethodSource("cqlStream")
  public void testNormalizeCqlUrlArgument(TestData data) {
    assertEquals(data.expected, FormatUtility.normalizeCqlUrlArgument(data.input));
  }

  @ParameterizedTest
  @MethodSource("postalCodeStream")
  public void testNormalizePostalCode(TestData data) {
    assertEquals(data.expected, FormatUtility.normalizePostalCode(data.input));
  }

  @ParameterizedTest
  @MethodSource("phoneNumberStream")
  public void testNormalizePhoneNumber(TestData data) {
    assertEquals(data.expected, FormatUtility.normalizePhoneNumber(data.input));
  }

}
