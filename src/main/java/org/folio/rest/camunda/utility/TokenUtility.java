package org.folio.rest.camunda.utility;

import java.net.HttpCookie;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * Provide FOLIO token processing functionality.
 *
 * This primarily supports the OKAPI `X-Okapi-Token` and the RTR HTTP Header Cookie tokens.
 *
 * This is intended to be extendable into newer FOLIO functionality, such as seen in Eureka. 
 */
@Component
public final class TokenUtility {

  private static final Logger LOG = LoggerFactory.getLogger(TokenUtility.class);

  private static String accessCookieName; 

  private static String refreshCookieName; 

  private static String tokenHeaderName;

  /**
   * Empty initializer to explicitly prevent initialization.
   *
   * This class is intended to be run using public static methods and should not be instantiated.
   */
  private TokenUtility() {
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

    LOG.debug("Initialized FOLIO Access Token cookie name to {}.", accessCookieName);
  }

  /**
   * Get the Access Token cookie name.
   *
   * @return The name.
   */
  public static String getAccessCookieName() {
    return accessCookieName;
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

  /**
   * Get the Refresh Token cookie name.
   *
   * @return The name.
   */
  public static String getRefreshCookieName() {
    return refreshCookieName;
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
   * Get the OKAPI Token header name.
   *
   * @return The name.
   */
  public static String getTokenHeaderName() {
    return tokenHeaderName;
  }

  /**
   * Parse an array of cookie header and return the first Access Cookie.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   *
   * @return A HttpCookie object on success or NULL on failure.
   */
  public static HttpCookie parseAccessCookie(String[] headers) {
    if (headers != null) {
      for (String header : headers) {
        HttpCookie cookie = parseAccessCookie(header);

        if (cookie != null) {
          return cookie;
        }
      }
    }

    return null;
  }

  /**
   * Parse an array of cookie header and return the first Access Cookie.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return A HttpCookie object on success or NULL on failure.
   */
  public static HttpCookie parseAccessCookie(String header) {
    return parseCookieName(header, getAccessCookieName());
  }

  /**
   * Parse an array of cookie header and return the first Refresh Cookie.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   *
   * @return A HttpCookie object on success or NULL on failure.
   */
  public static HttpCookie parseRefreshCookie(String[] headers) {
    if (headers != null) {
      for (String header : headers) {
        HttpCookie cookie = parseRefreshCookie(header);

        if (cookie != null) {
          return cookie;
        }
      }
    }

    return null;
  }

  /**
   * Parse an array of cookie header and return the first Refresh Cookie.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return A HttpCookie object on success or NULL on failure.
   */
  public static HttpCookie parseRefreshCookie(String header) {
    return parseCookieName(header, getRefreshCookieName());
  }

  /**
   * Parse a single cookie header and return the cookie object that matches the given name.
   *
   * @param name The cookie name to check for and return.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return A HttpCookie object on success or NULL on failure.
   */
  public static HttpCookie parseCookieName(String header, String name) {
    if (header != null) {
      List<HttpCookie> cookies = HttpCookie.parse(header);

      if (cookies != null) {
        for (HttpCookie cookie : cookies) {
          if (cookie.getName().equalsIgnoreCase(name)) {
            return cookie;
          }
        }
      }
    }

    return null;
  }

}
