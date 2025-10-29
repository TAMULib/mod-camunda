package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.HttpCookie;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Base test class for testing the TokenUtility.
 *
 * This is done because the properties must be defined using @SpringBootTest properties.
 * Doing this requires multiple test classes.
 */
abstract class TestTokenUtilityBase {

  private static final String TOKEN_HEADER_VALUE = "=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nX2FkbWluIiwidXNlcl9pZCI6IjczZjgxYTFlLTk0ZGMtNDk3NS1hYjdlLTM3YWM0NGUwNmIyYiIsInR5cGUiOiJhY2Nlc3MiLCJleHAiOjE2OTUzOTMwMTAsImlhdCI6MTY5NTM5MjQxMCwidGVuYW50IjoidGVzdGxpYjE0In0.PqMCOD9ghOPQE7kzD1hYd09otNgOU0T4C1oMHkpn2no";

  private static final String ACCESS_COOKIE_BASE = TOKEN_HEADER_VALUE + "; Max-Age=600; Expires=Fri, 22 Sep 2023 14:30:10 GMT; Path=/; Secure; HTTPOnly; SameSite=None";

  private static final String REFRESH_COOKIE_BASE = "=eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..hA3f_Sa2xV7Vo7Np.j9P0oTsggs0r48kMNa5f-M8JrRRAdXx0hYJZp4ecLfNFHA8ee2WtZZQ_P9EGgktqokHOIfVsxQewLbXF04-O2xtlc6i8tCxii3Hv8wT8HmYqcWKAZ6g3UYXS81QS3fYdGsH6DTV_c8hU77knHKGZo3YLbNv5-Qs7en7EhzCEyo79x7hQFfVUrgY7YbKixKkVkRvshTt_nSD2S0T44-H-KC-vmKvn7yLu9W-pCCU4E37NFjBWV9vjK6NiOOa_H-9ZfFo1pDoMTpfrc1KX824LVRAAmvVjLxMQYDtOVZPTSdIO888.v6Pe4Wm-kOPEjmJaiZ32kQ; Max-Age=604800; Expires=Fri, 29 Sep 2023 14:20:10 GMT; Path=/authn; Secure; HTTPOnly; SameSite=None";

  /**
   * A header to use in unit tests that is expected to not be matched.
   */
  protected static final String OTHER_HEADER_VALUE = "other=value; Max-Age=604800; Expires=Fri, 29 Sep 2025 14:20:10 GMT; Path=/other_path";

  /**
   * Run the get Access token test.
   *
   * This test requires the implementing class to define provideSingleAccessHeader().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideSingleAccessHeader")
  void testGetAccessToken(String header, HttpCookie expect) {
    String result = TokenUtility.getAccessToken(header);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.getValue(), result.toString());
    }
  }

  /**
   * Run the get Access tokens test.
   *
   * This test requires the implementing class to define provideMultipleAccessHeaders().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideMultipleAccessHeaders")
  void testGetAccessTokens(String[] headers, HttpCookie expect) {
    String result = TokenUtility.getAccessTokens(headers);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.getValue(), result.toString());
    }
  }

  /**
   * Run the get Refresh token test.
   *
   * This test requires the implementing class to define provideSingleRefreshHeader().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideSingleRefreshHeader")
  void testGetRefreshToken(String header, HttpCookie expect) {
    String result = TokenUtility.getRefreshToken(header);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.getValue(), result.toString());
    }
  }

  /**
   * Run the get Refresh tokens test.
   *
   * This test requires the implementing class to define provideMultipleRefreshHeaders().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideMultipleRefreshHeaders")
  void testGetRefreshTokens(String[] headers, HttpCookie expect) {
    String result = TokenUtility.getRefreshTokens(headers);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.getValue(), result.toString());
    }
  }

  /**
   * Run the parse Access cookie test.
   *
   * This test requires the implementing class to define provideSingleAccessHeader().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideSingleAccessHeader")
  void testParseAccessCookie(String header, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseAccessCookie(header);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Run the parse Access cookies test.
   *
   * This test requires the implementing class to define provideMultipleAccessHeaders().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideMultipleAccessHeaders")
  void testParseAccessCookies(String[] headers, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseAccessCookies(headers);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Run the parse Refresh cookie test.
   *
   * This test requires the implementing class to define provideSingleRefreshHeader().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideSingleRefreshHeader")
  void testParseRefreshCookie(String header, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseRefreshCookie(header);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Run the parse Refresh cookies test.
   *
   * This test requires the implementing class to define provideMultipleRefreshHeaders().
   *
   * @param headers The sets of headers.
   * @param expect  The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideMultipleRefreshHeaders")
  void testParseRefreshCookies(String[] headers, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseRefreshCookies(headers);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Run the parse cookie name test.
   *
   * @param header The header.
   * @param name   The name to match.
   * @param expect The expected cookie result.
   */
  @ParameterizedTest
  @MethodSource("provideCookieNames")
  void testParseCookieName(String header, String name, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseCookieName(header, name);

    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Get the Access cookie name for testing.
   *
   * @return The name.
   */
  abstract protected String getAccessCookieName();

  /**
   * Get the Refresh cookie name for testing.
   *
   * @return The name.
   */
  abstract protected String getRefreshCookieName();

  /**
   * Get the token header name for testing.
   *
   * @return The name.
   */
  abstract protected String getTokenHeaderName();

  /**
   * Get the Access cookie.
   *
   * @return The entire cookie.
   */
  protected String getAccessCookie() {
    return getAccessCookieName() + ACCESS_COOKIE_BASE;
  }

  /**
   * Get the Refresh cookie.
   *
   * @return The entire cookie.
   */
  protected String getRefreshCookie() {
    return getRefreshCookieName() + REFRESH_COOKIE_BASE;
  }

  /**
   * Get the token.
   *
   * @return The header value.
   */
  protected String getToken() {
    return TOKEN_HEADER_VALUE;
  }

  /**
   * Builder for stream parameters for testing access headers.
   *
   * @param instance The instantiated instance containing the implemented abstract methods.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildProvideMultipleAccessHeaders(TestTokenUtilityBase instance) {
    final String[] accessStrs1 = new String[] { instance.getAccessCookie() };
    final String[] accessStrs2 = new String[] { instance.getAccessCookie(), OTHER_HEADER_VALUE };
    final String[] accessStrs3 = new String[] { OTHER_HEADER_VALUE, instance.getAccessCookie() };
    final String[] refreshStrs1 = new String[] { instance.getRefreshCookie() };
    final String[] refreshStrs2 = new String[] { instance.getRefreshCookie(), OTHER_HEADER_VALUE };
    final String[] refreshStrs3 = new String[] { OTHER_HEADER_VALUE, instance.getRefreshCookie() };
    final HttpCookie nullCookie = null;

    final Stream<Arguments> basic = buildProvideMultipleBasicHeaders();

    HttpCookie cookie = HttpCookie.parse(instance.getAccessCookie()).getFirst();

    final Stream<Arguments> specific = Stream.of(
      Arguments.of(accessStrs1, cookie),
      Arguments.of(accessStrs2, cookie),
      Arguments.of(accessStrs3, cookie),
      Arguments.of(refreshStrs1, nullCookie),
      Arguments.of(refreshStrs2, nullCookie),
      Arguments.of(refreshStrs3, nullCookie)
    );

    return Stream.concat(basic, specific);
  }

  /**
   * Get standard stream containing the NULL and empty tests.
   *
   * This is intended to be merged with more specific tests unique to each implementing class.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildProvideMultipleBasicHeaders() {
    final String[] nullStrs = null;
    final String[] emptyStrs = new String[] {};
    final String[] otherStrs = new String[] { OTHER_HEADER_VALUE };
    final HttpCookie nullCookie = null;

    return Stream.of(
      Arguments.of(nullStrs,  nullCookie),
      Arguments.of(emptyStrs, nullCookie),
      Arguments.of(otherStrs, nullCookie)
    );
  }

  /**
   * Builder for stream parameters for testing refresh headers.
   *
   * @param instance The instantiated instance containing the implemented abstract methods.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildProvideMultipleRefreshHeaders(TestTokenUtilityBase instance) {
    final String[] accessStrs1 = new String[] { instance.getAccessCookie() };
    final String[] accessStrs2 = new String[] { instance.getAccessCookie(), OTHER_HEADER_VALUE };
    final String[] accessStrs3 = new String[] { OTHER_HEADER_VALUE, instance.getAccessCookie() };
    final String[] refreshStrs1 = new String[] { instance.getRefreshCookie() };
    final String[] refreshStrs2 = new String[] { instance.getRefreshCookie(), OTHER_HEADER_VALUE };
    final String[] refreshStrs3 = new String[] { OTHER_HEADER_VALUE, instance.getRefreshCookie() };
    final HttpCookie nullCookie = null;

    final Stream<Arguments> basic = buildProvideMultipleBasicHeaders();

    HttpCookie cookie = HttpCookie.parse(instance.getRefreshCookie()).getFirst();

    final Stream<Arguments> specific = Stream.of(
      Arguments.of(accessStrs1, nullCookie),
      Arguments.of(accessStrs2, nullCookie),
      Arguments.of(accessStrs3, nullCookie),
      Arguments.of(refreshStrs1, cookie),
      Arguments.of(refreshStrs2, cookie),
      Arguments.of(refreshStrs3, cookie)
    );

    return Stream.concat(basic, specific);
  }

  /**
   * Builder for stream parameters for testing access header.
   *
   * @param instance The instantiated instance containing the implemented abstract methods.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildProvideSingleAccessHeader(TestTokenUtilityBase instance) {
    final HttpCookie nullCookie = null;

    HttpCookie cookie = HttpCookie.parse(instance.getAccessCookie()).getFirst();

    return Stream.of(
      Arguments.of(instance.getAccessCookie(), cookie),
      Arguments.of(OTHER_HEADER_VALUE, nullCookie),
      Arguments.of(instance.getRefreshCookie(), nullCookie),
      Arguments.of(null, nullCookie)
    );
  }

  /**
   * Builder for stream parameters for testing refresh header.
   *
   * @param instance The instantiated instance containing the implemented abstract methods.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildProvideSingleRefreshHeader(TestTokenUtilityBase instance) {
    final HttpCookie nullCookie = null;

    HttpCookie cookie = HttpCookie.parse(instance.getRefreshCookie()).getFirst();

    return Stream.of(
      Arguments.of(instance.getAccessCookie(), nullCookie),
      Arguments.of(OTHER_HEADER_VALUE, nullCookie),
      Arguments.of(instance.getRefreshCookie(), cookie),
      Arguments.of(null, nullCookie)
    );
  }

  /**
   * Stream parameters for testing parse cookie name.
   *
   * @return
   *   The test method parameters:
   *     - String header (the single header).
   *     - String name (the name to match).
   *     - HttpCookie expect (the expected result).
   */
  private static Stream<Arguments> provideCookieNames() {
    final HttpCookie nullCookie = null;

    final String name1 = "name1";
    final String name2 = "name2";
    final String name3 = "other";

    final String header1 = name1 + "=" + ACCESS_COOKIE_BASE;
    final String header2 = name2 + "=" + ACCESS_COOKIE_BASE;
    final String header3 = OTHER_HEADER_VALUE;

    HttpCookie cookie1 = HttpCookie.parse(header1).getFirst();
    HttpCookie cookie2 = HttpCookie.parse(header2).getFirst();
    HttpCookie cookie3 = HttpCookie.parse(header3).getFirst();

    return Stream.of(
      Arguments.of(header1, name1, cookie1),
      Arguments.of(header2, name1, nullCookie),
      Arguments.of(header3, name1, nullCookie),
      Arguments.of(header1, name2, nullCookie),
      Arguments.of(header2, name2, cookie2),
      Arguments.of(header3, name2, nullCookie),
      Arguments.of(header1, name3, nullCookie),
      Arguments.of(header2, name3, nullCookie),
      Arguments.of(header3, name3, cookie3),
      Arguments.of("",      name1, nullCookie),
      Arguments.of("",      name2, nullCookie),
      Arguments.of("",      name3, nullCookie),
      Arguments.of(null,    name1, nullCookie),
      Arguments.of(null,    name2, nullCookie),
      Arguments.of(null,    name3, nullCookie),
      Arguments.of(header1, "",    nullCookie),
      Arguments.of(header2, "",    nullCookie),
      Arguments.of(header3, "",    nullCookie),
      Arguments.of(header1, null,  nullCookie),
      Arguments.of(header2, null,  nullCookie),
      Arguments.of(header3, null,  nullCookie),
      Arguments.of("",      "",    nullCookie),
      Arguments.of("",      null,  nullCookie),
      Arguments.of(null,    "",    nullCookie),
      Arguments.of(null,    null,  nullCookie)
    );
  }

}
