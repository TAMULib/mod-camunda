package org.folio.rest.camunda.utility;

import java.net.HttpCookie;
import java.util.List;
import org.folio.rest.camunda.config.TokenConfig;

/**
 * Provide FOLIO token processing functionality.
 *
 * This primarily supports the OKAPI `X-Okapi-Token` and the RTR HTTP Header Cookie tokens.
 *
 * This is intended to be extendable into newer FOLIO functionality, such as seen in Eureka.
 */
public final class TokenUtility {

  /**
   * Empty initializer to explicitly prevent initialization.
   *
   * This class is intended to be run using public static methods and should not be instantiated.
   */
  private TokenUtility() {
  }

  /**
   * Get the Access token from a single cookie header.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return The Access token, if found, or NULL otherwise.
   */
  public static String getAccessToken(String header) {
    final HttpCookie cookie = parseAccessCookie(header);

    return cookie == null ? null : cookie.getValue();
  }

  /**
   * Get first Access token from an array of cookie headers.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   *
   * @return The Access token, if found, or NULL otherwise.
   */
  public static String getAccessTokens(String[] headers) {
    if (headers != null) {
      for (String header : headers) {
        final String token = getAccessToken(header);

        if (token != null) {
          return token;
        }
      }
    }

    return null;
  }

  /**
   * Get the Refresh token from a single cookie header.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return The Refresh token, if found, or NULL otherwise.
   */
  public static String getRefreshToken(String header) {
    final HttpCookie cookie = parseRefreshCookie(header);

    return cookie == null ? null : cookie.getValue();
  }

  /**
   * Get the first Refresh token from an array of cookie headers.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   *
   * @return The Refresh token, if found, or NULL otherwise.
   */
  public static String getRefreshTokens(String[] headers) {
    if (headers != null) {
      for (String header : headers) {
        final String token = getRefreshToken(header);

        if (token != null) {
          return token;
        }
      }
    }

    return null;
  }

  /**
   * Parse a single cookie header string and return the HttpCookie representation if it is an Access cookie.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return A HttpCookie object, if found, or NULL otherwise.
   */
  public static HttpCookie parseAccessCookie(String header) {
    return parseCookieName(header, TokenConfig.getAccessCookieName());
  }

  /**
   * Parse an array of cookie header and return the first Access Cookie.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   *
   * @return A HttpCookie object, if found, or NULL otherwise.
   */
  public static HttpCookie parseAccessCookies(String[] headers) {
    if (headers != null) {
      for (String header : headers) {
        final HttpCookie cookie = parseAccessCookie(header);

        if (cookie != null) {
          return cookie;
        }
      }
    }

    return null;
  }

  /**
   * Parse a single cookie header string and return the HttpCookie representation if it is a Refresh cookie.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return A HttpCookie object, if found, or NULL otherwise.
   */
  public static HttpCookie parseRefreshCookie(String header) {
    return parseCookieName(header, TokenConfig.getRefreshCookieName());
  }

  /**
   * Parse an array of cookie header and return the first Refresh Cookie.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   *
   * @return A HttpCookie object, if found, or NULL otherwise.
   */
  public static HttpCookie parseRefreshCookies(String[] headers) {
    if (headers != null) {
      for (String header : headers) {
        final HttpCookie cookie = parseRefreshCookie(header);

        if (cookie != null) {
          return cookie;
        }
      }
    }

    return null;
  }

  /**
   * Parse a single cookie header and return the cookie object that matches the given name.
   *
   * @param name The cookie name to check for and return.
   *
   * @param header The cookie header to parse. This is usually a `Set-Cookie` header.
   *
   * @return A HttpCookie object, if found, or NULL otherwise.
   */
  public static HttpCookie parseCookieName(String header, String name) {
    if (header != null && !header.isBlank()) {
      final List<HttpCookie> cookies = HttpCookie.parse(header);

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
