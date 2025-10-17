package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.HttpCookie;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

abstract class TokenUtilityBaseTest {

  private static final String TOKEN_HEADER_VALUE = "=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nX2FkbWluIiwidXNlcl9pZCI6IjczZjgxYTFlLTk0ZGMtNDk3NS1hYjdlLTM3YWM0NGUwNmIyYiIsInR5cGUiOiJhY2Nlc3MiLCJleHAiOjE2OTUzOTMwMTAsImlhdCI6MTY5NTM5MjQxMCwidGVuYW50IjoidGVzdGxpYjE0In0.PqMCOD9ghOPQE7kzD1hYd09otNgOU0T4C1oMHkpn2no";

  private static final String ACCESS_COOKIE_BASE = TOKEN_HEADER_VALUE + "; Max-Age=600; Expires=Fri, 22 Sep 2023 14:30:10 GMT; Path=/; Secure; HTTPOnly; SameSite=None";

  private static final String REFRESH_COOKIE_BASE = "=eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..hA3f_Sa2xV7Vo7Np.j9P0oTsggs0r48kMNa5f-M8JrRRAdXx0hYJZp4ecLfNFHA8ee2WtZZQ_P9EGgktqokHOIfVsxQewLbXF04-O2xtlc6i8tCxii3Hv8wT8HmYqcWKAZ6g3UYXS81QS3fYdGsH6DTV_c8hU77knHKGZo3YLbNv5-Qs7en7EhzCEyo79x7hQFfVUrgY7YbKixKkVkRvshTt_nSD2S0T44-H-KC-vmKvn7yLu9W-pCCU4E37NFjBWV9vjK6NiOOa_H-9ZfFo1pDoMTpfrc1KX824LVRAAmvVjLxMQYDtOVZPTSdIO888.v6Pe4Wm-kOPEjmJaiZ32kQ; Max-Age=604800; Expires=Fri, 29 Sep 2023 14:20:10 GMT; Path=/authn; Secure; HTTPOnly; SameSite=None";

  /**
   * A header to use in unit tests that is expected to not be matched.
   */
  protected static final String OTHER_HEADER_VALUE = "other=value; Max-Age=604800; Expires=Fri, 29 Sep 2025 14:20:10 GMT; Path=/other_path";

  @Test
  void testGetAccessCookieName() {
    assertEquals(getAccessCookieName(), TokenUtility.getAccessCookieName());
  }

  @Test
  void testGetRefreshCookieName() {
    assertEquals(getRefreshCookieName(), TokenUtility.getRefreshCookieName());
  }

  @Test
  void testGetTokenHeaderName() {
    assertEquals(getTokenHeaderName(), TokenUtility.getTokenHeaderName());
  }

  /**
   * Run the parse access cookie test.
   *
   * This test requires the implementing class to define provideMultipleAccessHeaderSets().
   *
   * @param headers The sets of headers
   * @param expect  The expected result
   */
  @ParameterizedTest
  @MethodSource("provideMultipleAccessHeaderSets")
  void testParseAccessCookie(String[] headers, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseAccessCookie(headers);
    
    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Run the parse refresh cookie test.
   *
   * This test requires the implementing class to define provideMultipleRefreshHeaderSets().
   *
   * @param headers The sets of headers
   * @param expect  The expected result
   */
  @ParameterizedTest
  @MethodSource("provideMultipleRefreshHeaderSets")
  void testParseRefreshCookie(String[] headers, HttpCookie expect) {
    HttpCookie result = TokenUtility.parseRefreshCookie(headers);
    
    if (expect == null) {
      assertNull(result);
    } else {
      assertNotNull(result);
      assertEquals(expect.toString(), result.toString());
    }
  }

  /**
   * Get the access cookie name for testing.
   *
   * @return The name.
   */
  abstract protected String getAccessCookieName();

  /**
   * Get the refresh cookie name for testing.
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
   * Get the access cookie.
   *
   * @return The entire cookie.
   */
  protected String getAccessCookie() {
    return getAccessCookieName() + ACCESS_COOKIE_BASE;
  }

  /**
   * Get the refresh cookie.
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
   * Get standard stream containing the NULL and empty tests.
   *
   * This is intended to be merged with more specific tests unique to each implementing class.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildBasicMultipleHeaderSets() {
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
   * Builder for stream parameters for testing access headers.
   *
   * @param instance The instantiated instance containing the implemented abstract methods.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  protected static Stream<Arguments> buildProvideMultipleAccessHeaderSets(TokenUtilityBaseTest instance) {
    final String[] accessStrs1 = new String[] { instance.getAccessCookie() };
    final String[] accessStrs2 = new String[] { instance.getAccessCookie(), OTHER_HEADER_VALUE };
    final String[] accessStrs3 = new String[] { OTHER_HEADER_VALUE, instance.getAccessCookie() };

    final Stream<Arguments> basic = buildBasicMultipleHeaderSets();

    HttpCookie cookie = HttpCookie.parse(instance.getAccessCookie()).getFirst();

    final Stream<Arguments> specific = Stream.of(
      Arguments.of(accessStrs1, cookie),
      Arguments.of(accessStrs2, cookie),
      Arguments.of(accessStrs3, cookie)
    );
    
    return Stream.concat(basic, specific);
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
  protected static Stream<Arguments> buildProvideMultipleRefreshHeaderSets(TokenUtilityBaseTest instance) {
    final String[] refreshStrs1 = new String[] { instance.getRefreshCookie() };
    final String[] refreshStrs2 = new String[] { instance.getRefreshCookie(), OTHER_HEADER_VALUE };
    final String[] refreshStrs3 = new String[] { OTHER_HEADER_VALUE, instance.getRefreshCookie() };

    final Stream<Arguments> basic = buildBasicMultipleHeaderSets();

    HttpCookie cookie = HttpCookie.parse(instance.getRefreshCookie()).getFirst();

    final Stream<Arguments> specific = Stream.of(
      Arguments.of(refreshStrs1, cookie),
      Arguments.of(refreshStrs2, cookie),
      Arguments.of(refreshStrs3, cookie)
    );

    return Stream.concat(basic, specific);
  }

}
