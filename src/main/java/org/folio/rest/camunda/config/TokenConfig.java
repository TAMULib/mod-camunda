package org.folio.rest.camunda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenConfig {

  private static final Logger LOG = LoggerFactory.getLogger(TokenConfig.class);

  private static String accessCookieName;

  private static String tokenHeaderName;

  private static String refreshCookieName;

  /**
   * Provide a static way get the access cookie name value.
   *
   * The yaml file names this `okapi.auth.accessCookieName`.
   * The environment variable for this is `OKAPI_AUTH_ACCESSCOOKIENAME`.
   *
   * The OkapiRestTemplate design prevents auto-injection because non-Java code will run the static methods via the TokenUtility.
   * Load the settings in such a way that a static method may access settings injected by Spring.
   *
   * @return The cookie header.
   */
  public static String getAccessCookieName() {
    return accessCookieName;
  }

  /**
   * Provide a static way get the tenant header name value.
   *
   * The yaml file names this `okapi.auth.tokenHeaderName`.
   * The environment variable for this is `OKAPI_AUTH_TOKENHEADERNAME`.
   *
   * The OkapiRestTemplate design prevents auto-injection because non-Java code will run the static methods via the TokenUtility.
   * Load the settings in such a way that a static method may access settings injected by Spring.
   *
   * @return The token header.
   */
  public static String getTokenHeaderName() {
    return tokenHeaderName;
  }

  /**
   * Provide a static way get the refresh cookie name value.
   *
   * The yaml file names this `okapi.auth.refreshCookieName`.
   * The environment variable for this is `OKAPI_AUTH_REFRESHCOOKIENAME`.
   *
   * The OkapiRestTemplate design prevents auto-injection because non-Java code will run the static methods via the TokenUtility.
   * Load the settings in such a way that a static method may access settings injected by Spring.
   *
   * @return The cookie header.
   */
  public static String getRefreshCookieName() {
    return refreshCookieName;
  }

  /**
   * Assign the Access Token cookie name.
   *
   * This is intended only to be called by Spring.
   *
   * @param value The value to assign.
   */
  @Value("${okapi.auth.accessCookieName:folioAccessToken}")
  protected void setAccessCookieName(String value) {
    accessCookieName = value;

    LOG.info("Initialized FOLIO Access Token cookie name to {}.", accessCookieName);
  }

  /**
   * Assign the OKAPI Token header name.
   *
   * This is intended only to be called by Spring.
   *
   * @param value The value to assign.
   */
  @Value("${okapi.auth.tokenHeaderName:X-Okapi-Token}")
  protected void setTokenHeaderName(String value) {
    tokenHeaderName = value;

    LOG.debug("Initialized FOLIO OKAPI Token header name to {}.", tokenHeaderName);
  }

  /**
   * Assign the Refresh Token cookie name.
   *
   * This is intended only to be called by Spring.
   *
   * @param value The value to assign.
   */
  @Value("${okapi.auth.refreshCookieName:folioRefreshToken}")
  protected void setRefreshCookieName(String value) {
    refreshCookieName = value;

    LOG.debug("Initialized FOLIO Refresh Token cookie name to {}.", refreshCookieName);
  }

}
