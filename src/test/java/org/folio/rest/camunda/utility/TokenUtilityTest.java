package org.folio.rest.camunda.utility;

import java.util.stream.Stream;
import org.folio.rest.camunda.config.TokenConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
  classes = {
    TokenConfig.class,
    TokenUtility.class
  }
 )
@ExtendWith(MockitoExtension.class)
class TokenUtilityTest extends TestTokenUtilityBase {

  private static final String ACCESS_COOKIE_NAME = "folioAccessToken";

  private static final String REFRESH_COOKIE_NAME = "folioRefreshToken";

  private static final String TOKEN_HEADER_NAME = "X-Okapi-Token";

  @Override
  protected String getAccessCookieName() {
    return ACCESS_COOKIE_NAME;
  }

  @Override
  protected String getRefreshCookieName() {
    return REFRESH_COOKIE_NAME;
  }

  @Override
  protected String getTokenHeaderName() {
    return TOKEN_HEADER_NAME;
  }

  /**
   * Stream parameters for testing access headers.
   *
   * NOTE: This has "unused" suppress warnings because it is used via MethodSource in the abstract class.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> provideMultipleAccessHeaders() {
    return buildProvideMultipleAccessHeaders(new TokenUtilityTest());
  }

  /**
   * Stream parameters for testing access headers.
   *
   * NOTE: This has "unused" suppress warnings because it is used via MethodSource in the abstract class.
   *
   * @return
   *   The test method parameters:
   *     - String[] headers (the sets of headers).
   *     - HttpCookie expect (the expected result).
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> provideMultipleRefreshHeaders() {
    return buildProvideMultipleRefreshHeaders(new TokenUtilityTest());
  }

  /**
   * Stream parameters for testing access header.
   *
   * NOTE: This has "unused" suppress warnings because it is used via MethodSource in the abstract class.
   *
   * @return
   *   The test method parameters:
   *     - String header (the single header).
   *     - HttpCookie expect (the expected result).
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> provideSingleAccessHeader() {
    return buildProvideSingleAccessHeader(new TokenUtilityTest());
  }

  /**
   * Stream parameters for testing refresh header.
   *
   * NOTE: This has "unused" suppress warnings because it is used via MethodSource in the abstract class.
   *
   * @return
   *   The test method parameters:
   *     - String header (the single header).
   *     - HttpCookie expect (the expected result).
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> provideSingleRefreshHeader() {
    return buildProvideSingleRefreshHeader(new TokenUtilityTest());
  }

}
