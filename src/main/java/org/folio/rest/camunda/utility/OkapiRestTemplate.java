package org.folio.rest.camunda.utility;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.folio.rest.camunda.config.CamundaTenantInit;
import org.folio.rest.camunda.config.TokenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * A specialized extension of Spring's {@link RestTemplate} designed specifically
 * for interacting with Okapi-based microservice platforms.
 *
 * <p>This custom REST template provides a fluent interface for configuring
 * Okapi-specific request parameters such as tenant identification,
 * authentication tokens, and base URL handling.</p>
 *
 * <p>The class follows the builder pattern, allowing for easy and flexible
 * configuration of REST template instances with Okapi-specific requirements.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Automatic addition of Okapi-specific headers</li>
 *   <li>Fluent configuration methods</li>
 *   <li>Simplified REST template creation</li>
 * </ul>
 * </p>
 *
 * @see RestTemplate
 * @see ClientHttpRequestInterceptor
 */
public class OkapiRestTemplate extends RestTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(OkapiRestTemplate.class);

  /**
   * Constructor.
   */
  public OkapiRestTemplate() {
    // Nothing to assign.
  }

  /**
   * Configures the REST template with tenant and authentication details.
   *
   * <p>This method sets up an HTTP request interceptor that automatically
   * adds Okapi-specific headers to each request:
   * <ul>
   *   <li>X-Okapi-Tenant header</li>
   *   <li>X-Okapi-Token header</li>
   *   <li>Accepted media types (JSON and plain text)</li>
   *   <li>Content type (JSON)</li>
   * </ul>
   * </p>
   *
   * @param tenant the Okapi tenant identifier (must not be null)
   * @param token the authentication token (must not be null)
   * @return the configured {@code OkapiRestTemplate} instance
   * @throws IllegalArgumentException if tenant or token is null or empty
   */
  public OkapiRestTemplate with(@NonNull String tenant, @NonNull String token) {
    this.setInterceptors(Collections.singletonList(new ClientHttpRequestInterceptor() {
      @Override
      public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
          throws IOException {
        HttpHeaders headers = request.getHeaders();

        headers.set(CamundaTenantInit.getHeaderName(), tenant);
        headers.set(TokenConfig.getTokenHeaderName(), token);

        headers.setAccept(Arrays.asList(APPLICATION_JSON, TEXT_PLAIN));
        headers.setContentType(APPLICATION_JSON);

        LOG.debug("Sending Okapi request for tenant {}: {} {}", tenant, request.getMethod(), request.getURI());

        return execution.execute(request, body);
      }
    }));

    return this;
  }

  /**
   * Configures the base URL for all requests made through this REST template.
   *
   * <p>This method sets up a {@link DefaultUriBuilderFactory} with the provided
   * Okapi URL, which will be used as the base for all subsequent REST requests.</p>
   *
   * @param okapiUrl the base URL for the Okapi service (must not be null)
   * @return the configured {@code OkapiRestTemplate} instance
   * @throws IllegalArgumentException if the provided URL is invalid
   */
  public OkapiRestTemplate at(@NonNull String okapiUrl) {
    DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(okapiUrl);
    this.setUriTemplateHandler(factory);

    return this;
  }

}
