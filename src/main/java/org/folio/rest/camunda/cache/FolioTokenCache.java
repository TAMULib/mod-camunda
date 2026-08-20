package org.folio.rest.camunda.cache;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.folio.rest.camunda.config.TokenConfig.getAccessCookieName;
import static org.folio.rest.camunda.config.TokenConfig.getRefreshCookieName;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.LITERAL;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.SECURE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.camunda.client.FolioClient;
import org.folio.rest.camunda.config.FolioEnvConfig;
import org.folio.rest.camunda.config.FolioGatewayConfig;
import org.folio.rest.camunda.exception.FolioAuthException;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.folio.rest.camunda.record.FolioErrorsRecord;
import org.folio.rest.camunda.request.FolioLoginRequest;
import org.folio.rest.camunda.response.FolioLoginResponse;
import org.folio.rest.camunda.utility.ClockUtility;
import org.folio.rest.camunda.utility.DateTimeUtility;
import org.folio.spring.tenant.properties.TenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
/**
 * A caching mechanism that atomically fetches expiring FOLIO-specific tokens.
 */
@Component
public class FolioTokenCache {

  private static final Logger LOG = LoggerFactory.getLogger(FolioTokenCache.class);

  /**
   * Expires HTTP cookie field name.
   */
  public static final String EXPIRES = "expires";

  /**
   * FOLIO environment configuration Access Token field name.
   */
  public static final String FOLIO_ACCESS_TOKEN = "folioAccessToken";

  /**
   * FOLIO environment configurationAccess Token expiration date as a UNIX time stamp field name.
   */
  public static final String FOLIO_ACCESS_TOKEN_EXPIRE = "folioAccessTokenExpire";

  /**
   * FOLIO environment configuration login path field name.
   */
  public static final String FOLIO_LOGIN_PATH = "folioLoginPath";

  /**
   * FOLIO environment configuration pass word field name.
   */
  public static final String FOLIO_PASS = "folioPass";

  /**
   * FOLIO Refresh Token value.
   */
  public static final String FOLIO_REFRESH_TOKEN = "folioRefreshToken";

  /**
   * FOLIO Refresh Token value.
   */
  public static final String FOLIO_REFRESH_TOKEN_EXPIRE = "folioRefreshTokenExpire";

  /**
   * FOLIO environment configuration user name field name.
   */
  public static final String FOLIO_USER = "folioUser";

  /**
   * FOLIO environment configuration gate way URL field name.
   */
  public static final String GATEWAY_URL = "gatewayUrl";

  /**
   * HTTP set-cookie header name.
   */
  public static final String SET_COOKIE = "set-cookie";

  private final ClockUtility clockUtility;

  private final FolioClient folioClient;

  private final FolioEnvConfig folioEnvConfig;

  private final FolioGatewayConfig folioGatewayConfig;

  private final ObjectMapper mapper;

  private final TenantProperties tenantProperties;

  /**
   * Class constructor.
   *
   * @param clockUtility     The clock utility.
   * @param FolioClient      The FOLIO client.
   * @param folioEnvConfig   The FOLIO environment configuration.
   * @param folioTokenConfig The FOLIO token configuration.
   * @param mapper           The JSON mapper.
   * @param tenantProperties The FOLIO tenant configuration.
   */
  public FolioTokenCache(
    ClockUtility clockUtility, FolioClient folioClient, FolioEnvConfig folioEnvConfig,
    FolioGatewayConfig folioGatewayConfig, ObjectMapper mapper, TenantProperties tenantProperties
  ) {

    this.clockUtility = clockUtility;
    this.folioClient = folioClient;
    this.folioEnvConfig = folioEnvConfig;
    this.folioGatewayConfig = folioGatewayConfig;
    this.mapper = mapper;
    this.tenantProperties = tenantProperties;
  }

  /**
   * Convert the string to a zoned date and time.
   *
   * Multiple common formatters are attempted due to DateTimeFormatter.
   *
   * @param value The date string.
   *
   * @return The converted zoned date and time.
   */
  public static ZonedDateTime parseZonedDateTime(String value) {

    final List<DateTimeFormatter> formatters = DateTimeUtility.getDateTimeFormatters();

    for (int i = 0; i < formatters.size(); i++) {
      try {
        final DateTimeFormatter formatter = formatters.get(i);

        return ZonedDateTime.parse(value, formatter).truncatedTo(MILLIS);
      } catch (DateTimeParseException e) {
        if (i == formatters.size() - 1) {
          throw e;
        }
      }
    }

    return ZonedDateTime.parse(value).truncatedTo(MILLIS);
  }

  /**
   * Ensure that the required items always exist.
   */
  @PostConstruct
  public void postConstruct() {

    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(true, FOLIO_ACCESS_TOKEN, SECURE, null));
    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(true, FOLIO_ACCESS_TOKEN_EXPIRE, LITERAL, null));
    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(true, FOLIO_REFRESH_TOKEN, SECURE, null));
    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(true, FOLIO_REFRESH_TOKEN_EXPIRE, LITERAL, null));
    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(true, GATEWAY_URL, LITERAL, null));

    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(false, FOLIO_PASS, SECURE, null));
    folioEnvConfig.initializeItem(new FolioEnvDefaultsItem(false, FOLIO_USER, LITERAL, null));
  }

  /**
   * Verify tokens are not expired and refresh if necessary.
   *
   * @param execution The execution data.
   *
   * @return The access token.
   */
  public String verifyTokens(final DelegateExecution execution) {

    final String accessValue = folioEnvConfig.retrieveItemValue(FOLIO_ACCESS_TOKEN_EXPIRE);
    final long accessTimestamp = accessValue == null ? 0L : Long.parseLong(accessValue);
    final Instant accessInstant = Instant.ofEpochMilli(accessTimestamp);
    final ZonedDateTime accessExpire = ZonedDateTime.ofInstant(accessInstant, Clock.systemDefaultZone().getZone());

    final Long offset = folioGatewayConfig.getTokenExpireOffset();
    final ZonedDateTime offsetTime = clockUtility.now().plusSeconds(offset);
    final boolean useCache = accessValue != null && accessExpire.isAfter(offsetTime);
    final String tenantId = getTenantId(execution);

    LOG.debug(
      "Now is '{}' and access expiration is '{}', {} FOLIO access token using tenant '{}'.",
      offsetTime,
      accessValue == null ? null : accessExpire,
      useCache ? "using cached" : "fetching new",
      tenantId
    );

    if (useCache) {
      return folioEnvConfig.retrieveItemValue(FOLIO_ACCESS_TOKEN);
    }

    final String folioUser = folioEnvConfig.retrieveItemValue(FOLIO_USER);
    final String gatewayUrl = folioEnvConfig.retrieveItemValue(GATEWAY_URL);
    final String loginPath = folioEnvConfig.retrieveItemValue(FOLIO_LOGIN_PATH);

    requestTokens(execution, gatewayUrl, loginPath, folioUser, tenantId); 

    return folioEnvConfig.retrieveItemValue(FOLIO_ACCESS_TOKEN);
  }

  /**
   * Extract the FOLIO tokens from the list of headers.
   *
   * @param headers An array of cookie headers. This is usually `Set-Cookie` headers.
   */
  private void extractFolioTokensByName(List<String> headers) {

    boolean foundAccess = false;
    boolean foundRefresh = false;

    for (String header : headers) {
      Boolean isAccess = null;
      String token = null;
      String expires = null;

      for (String field : header.split(";")) {
        final String[] parts = field.split("=", 2);

        if (parts.length > 1) {
          if (getAccessCookieName().equalsIgnoreCase(parts[0].trim())) {
            isAccess = true;
            token = parts[1].trim();
            foundAccess = true;
          } else if (getRefreshCookieName().equalsIgnoreCase(parts[0].trim())) {
            isAccess = false;
            token = parts[1].trim();
            foundRefresh = true;
          } else if (EXPIRES.equalsIgnoreCase(parts[0].trim())) {
            expires = parts[1].trim();
          }
        }
      }

      if (isAccess != null && token != null && expires != null) {
        final ZonedDateTime expire = DateTimeUtility.parseZonedDateTime(expires);

        if (Boolean.TRUE.equals(isAccess)) {
          folioEnvConfig.changeItemValue(FOLIO_ACCESS_TOKEN, "" + token);
          folioEnvConfig.changeItemValue(FOLIO_ACCESS_TOKEN_EXPIRE, "" + expire.toInstant().toEpochMilli());
        } else {
          folioEnvConfig.changeItemValue(FOLIO_REFRESH_TOKEN, "" + token);
          folioEnvConfig.changeItemValue(FOLIO_REFRESH_TOKEN_EXPIRE, "" + expire.toInstant().toEpochMilli());
        }
      }
    }

    if (!foundAccess || !foundRefresh) {
      final String detail = String.format(
        "The %s set-cookie header is NULL in the FOLIO login response", !foundAccess ? "access" : "refresh"
      );

      throw new FolioAuthException(String.format("Invalid data returned by authentication server; details: %s", detail));
    }
  }

  /**
   * Get the named value depending on if and how it is defined.
   *
   * This is necessary because properties like user or pass might not be exposed values.
   *
   * @param execution The execution data.
   * @param name      The variable name.
   *
   * @return The variable if found, otherwise an empty string.
   */
  private String getNamedValue(final DelegateExecution execution, final String name) {

    String value = null;

    if (execution.hasVariableLocal(name)) {
      value = (String) execution.getVariableLocal(name);
    } else if (execution.hasVariable(name)) {
      value = (String) execution.getVariable(name);
    } else {
      value = folioEnvConfig.retrieveItemValue(name);
    }

    return value == null ? "" : value;
  }

  /**
   * Get the Tenant ID.
   *
   * @param execution The execution data.
   *
   * @return A loaded tenant, a default, or an empty string as a fail safe.
   */
  private String getTenantId(final DelegateExecution execution) {

    String tenant = execution.getTenantId();

    if (tenant == null) {
      tenant = tenantProperties.getDefaultTenant();
    }

    return tenant == null ? "" : tenant;
  }

  /**
   * Fetch new tokens from FOLIO.
   *
   * @param execution  The execution data.
   * @param gatewayUrl The gateway URL.
   * @param loginPath  The log in path.
   * @param folioUser  The FOLIO user name.
   * @param tenantId   The FOLIO tenant.
   *
   * @return TRUE on success, FALSE otherwise.
   */
  private void requestTokens(final DelegateExecution execution, final String gatewayUrl, final String loginPath, final String folioUser, final String tenantId) {

    final String url = gatewayUrl + (loginPath == null ? "" : loginPath);
    URI uri = null;

    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      throwFolioAuthException(tenantId, gatewayUrl, loginPath, null, null, "Failed to build URL", e);
    }

    LOG.debug("Performing Gateway Log in to '{}' with tenant '{}' using FOLIO user '{}'.", url, tenantId, folioUser);

    try {
      final ResponseEntity<FolioLoginResponse> response = folioClient
        .postLogin(
          uri,
          Map.of(
            tenantProperties.getHeaderName(),
            tenantId
          ),
          new FolioLoginRequest(
            getNamedValue(execution, FOLIO_PASS),
            folioUser
          )
        );

      final List<String> cookies = response
        .getHeaders()
        .get(SET_COOKIE);

      extractFolioTokensByName(cookies == null ? new ArrayList<>() : cookies);
    } catch (ResourceAccessException e) {
      throwFolioAuthException(tenantId, gatewayUrl, loginPath, null, null, e.getMessage(), e);
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      throwFolioAuthException(tenantId, gatewayUrl, loginPath, e.getResponseHeaders(), e.getStatusCode(), e.getResponseBodyAsString(), e);
    }
  }

  /**
   * Throw FOLIO authentication exception.
   *
   * @param tenant       The tenant ID.
   * @param gatewayUrl   The gateway URL.
   * @param loginPath    The login path.
   * @param headers      (optional) The HTTP headers, if any.
   * @param statusCode   (optional) The HTTP response status code, if any.
   * @param responseBody (optional) The HTTP response body or response error message.
   * @param e            (optional) The exception to bundle.
   */
  private void throwFolioAuthException(
    final String tenant, final String gatewayUrl, final String loginPath, final HttpHeaders headers,
    final HttpStatusCode statusCode,final String responseBody, final Exception e
  ) {

    String body = responseBody;
    String rawResponseBody = responseBody;

    try {
      final FolioErrorsRecord errors = mapper.readValue(responseBody, FolioErrorsRecord.class);

      if (!errors.errors().isEmpty()) {
        body = errors.errors().getFirst().message();
      }
    } catch (Exception ignore) {
      final String folioDetail = LOG.isDebugEnabled()
        ? String.format(", deserialized exception is: '%s'", ignore.getMessage())
        : "";

      // Use raw values when the response body is unknown or for any other error.
      LOG.warn("FOLIO HTTP error response failed to deserialize, using raw values insead{}", folioDetail);
    }

    final String details = String.format(
      "%sTenant '%s' from '%s%s' with body response of%s: %s",
      statusCode == null ? "" : String.format("Got HTTP Response%s for ", statusCode),
      tenant,
      gatewayUrl == null ? "" : gatewayUrl,
      loginPath == null ? "" : loginPath,
      headers == null ? "" : String.format(" (type is %s)", headers.getContentType()),
      rawResponseBody
    );

    throw new FolioAuthException(
      String.format(
        "%s%s; details: %s",
        statusCode == null ? "" : String.format("Received HTTP %s: ", statusCode),
        body,
        details
      ),
      e
    );
  }

}
