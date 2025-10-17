package org.folio.rest.camunda.utility;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
  "okapi.auth.accessCookieName=TestAccessCookieName",
  "okapi.auth.refreshCookieName=TestRefreshCookieName",
  "okapi.auth.tokenHeaderName=TestTokenHeaderName"
})
@ExtendWith(MockitoExtension.class)
class TokenUtilityCustomSettingsTest extends TokenUtilityBaseTest {

  private static final String ACCESS_COOKIE_NAME = "TestAccessCookieName";

  private static final String REFRESH_COOKIE_NAME = "TestRefreshCookieName";

  private static final String TOKEN_HEADER_NAME = "TestTokenHeaderName";

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
  private static Stream<Arguments> provideMultipleAccessHeaderSets() {
    return buildProvideMultipleAccessHeaderSets(new TokenUtilityCustomSettingsTest());
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
  private static Stream<Arguments> provideMultipleRefreshHeaderSets() {
    return buildProvideMultipleRefreshHeaderSets(new TokenUtilityCustomSettingsTest());
  }

}
