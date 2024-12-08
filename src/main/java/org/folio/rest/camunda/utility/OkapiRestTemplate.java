package org.folio.rest.camunda.utility;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
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

  /**
   * HTTP header name for specifying the Okapi tenant identifier.
   */
  private static final String OKAPI_TENENT_HEADER = "X-Okapi-Tenant";

  /**
   * HTTP header name for providing the authentication token.
   */
  private static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";

  /**
   * Private constructor to enforce the use of the {@link #build()} method
   * for creating instances.
   *
   * <p>This ensures that instances are created through a controlled,
   * configurable process.</p>
   */
  private OkapiRestTemplate() {

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
   * @param tenant the Okapi tenant identifier
   * @param token the authentication token
   * @return the configured {@code OkapiRestTemplate} instance
   * @throws IllegalArgumentException if tenant or token is null or empty
   */
  public OkapiRestTemplate with(String tenant, String token) {
    this.setInterceptors(Collections.singletonList(new ClientHttpRequestInterceptor() {
      @Override
      public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
          throws IOException {
        HttpHeaders headers = request.getHeaders();

        headers.set(OKAPI_TENENT_HEADER, token);
        headers.set(OKAPI_TOKEN_HEADER, tenant);

        headers.setAccept(Arrays.asList(APPLICATION_JSON, TEXT_PLAIN));
        headers.setContentType(APPLICATION_JSON);

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
   * @param okapiUrl the base URL for the Okapi service
   * @return the configured {@code OkapiRestTemplate} instance
   * @throws IllegalArgumentException if the provided URL is invalid
   */
  public OkapiRestTemplate at(String okapiUrl) {
    DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(okapiUrl);
    this.setUriTemplateHandler(factory);

    return this;
  }

  /**
   * Creates a new instance of {@code OkapiRestTemplate}.
   *
   * <p>This static factory method provides a fluent way to create and
   * configure an Okapi-specific REST template. It follows the builder pattern,
   * allowing method chaining for configuration.</p>
   *
   * <p>Example usage:
   * <pre>{@code
   * OkapiRestTemplate restTemplate = OkapiRestTemplate.build()
   *     .with("tenant-id", "auth-token")
   *     .at("https://okapi.example.edu");
   * }</pre>
   * </p>
   *
   * @return a new, unconfigured {@code OkapiRestTemplate} instance
   */
  public static OkapiRestTemplate build() {
    return new OkapiRestTemplate();
  }

}
