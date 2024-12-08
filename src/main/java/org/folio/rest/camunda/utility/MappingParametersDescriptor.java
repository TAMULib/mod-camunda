package org.folio.rest.camunda.utility;

import java.lang.reflect.InvocationTargetException;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClientException;

/**
 * A utility class that describes and facilitates the retrieval of mapping
 * parameters from RESTful endpoints in a generic and type-safe manner.
 *
 * <p>This descriptor encapsulates the details required to fetch a specific type of
 * collection of parameters from a predefined REST endpoint, using the Okapi
 * REST template. It provides a flexible mechanism for dynamically retrieving
 * configuration parameters across different domain types.</p>
 *
 * <p>The class is designed to work with generic types, allowing it to handle
 * various collection and parameter types while maintaining type safety during
 * parameter retrieval.</p>
 *
 * @param <C> the type of the collection to be retrieved from the REST endpoint
 * @param <P> the type of the parameters within the collection
 * @see OkapiRestTemplate
 */
public class MappingParametersDescriptor<C, P> {

  /**
   * The {@link Class} representation of the collection type to be retrieved.
   *
   * <p>This class is used to specify the exact type of collection when performing
   * the REST request, ensuring type-safe deserialization of the response.</p>
   */
  private final Class<C> collectionType;

  /**
   * The REST endpoint path from which the parameters will be retrieved.
   *
   * <p>This path represents the complete URL endpoint (excluding the base URL)
   * from which the collection of parameters will be fetched.</p>
   */
  private final String path;

  /**
   * Constructs a new {@code MappingParametersDescriptor} with the specified
   * collection type and REST endpoint path.
   *
   * <p>This private constructor ensures that instances are created through
   * the static factory method {@link #of(Class, String)}, promoting a more
   * controlled and clear object creation process.</p>
   *
   * @param collectionType the {@link Class} representing the type of collection to retrieve
   * @param path the REST endpoint path for fetching the parameters
   * @throws IllegalArgumentException if the collection type or path is null
   */
  private MappingParametersDescriptor(@NonNull Class<C> collectionType, @NonNull String path) {
    this.collectionType = collectionType;
    this.path = path;
  }

  /**
   * Retrieves the parameters from the specified REST endpoint using the provided
   * {@link OkapiRestTemplate}.
   *
   * <p>This method performs a GET request to the predefined path, expecting a
   * response of the specified collection type. It leverages Spring's
   * {@link OkapiRestTemplate} to execute the HTTP request and extract the response body.</p>
   *
   * <p>If no response body is present, the method attempts to create a new instance
   * of the collection type using its default constructor. This ensures that even
   * in cases of empty responses, a valid collection is returned.</p>
   *
   * @param okapiRestTemplate the REST template used to perform the HTTP GET operation
   * @return the collection of parameters retrieved from the specified endpoint
   * @throws ParameterRetrievalException if there is an error during parameter retrieval,
   *         which could be caused by network issues, deserialization problems, or
   *         inability to instantiate the collection type
   */
  C getParameters(OkapiRestTemplate okapiRestRemplate) {
    try {
      ResponseEntity<C> response = okapiRestRemplate.getForEntity(path, collectionType);

      // Return response body if present, otherwise create a new instance
      return response.hasBody() ? response.getBody() : collectionType.getDeclaredConstructor().newInstance();
    } catch (RestClientException e) {
      throw new ParameterRetrievalException("Failed to retrieve parameters from path: " + path, e);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new ParameterRetrievalException("Unable to create default instance of " + collectionType.getSimpleName(), e);
    }
  }

  /**
   * Creates a new instance of {@code MappingParametersDescriptor} using a static
   * factory method.
   *
   * <p>This method provides a more readable and fluent way of creating
   * {@code MappingParametersDescriptor} instances, following the builder pattern.
   * It allows for concise and clear instantiation of descriptors.</p>
   *
   * <p>Example usage:
   * <pre>{@code
   * MappingParametersDescriptor<Locations, Location> descriptor =
   *     MappingParametersDescriptor.of(Locations.class, "/locations");
   * }</pre>
   * </p>
   *
   * @param <C> the type of the collection to be retrieved
   * @param <P> the type of the parameters within the collection
   * @param collectionType the {@link Class} representing the collection type
   * @param path the REST endpoint path for fetching the parameters
   * @return a new instance of {@code MappingParametersDescriptor}
   * @throws IllegalArgumentException if the collection type or path is null
   */
  public static <C, P> MappingParametersDescriptor<C, P> of(Class<C> collectionType, String path) {
    return new MappingParametersDescriptor<C, P>(collectionType, path);
  }

  /**
   * Custom exception for parameter retrieval failures.
   *
   * <p>This exception is thrown when there are issues retrieving or instantiating
   * parameter collections, providing detailed context about the failure.</p>
   */
  public static class ParameterRetrievalException extends RuntimeException {

    /**
     * Constructs a new {@code ParameterRetrievalException} with a detailed message
     * and the underlying cause.
     *
     * @param message a descriptive error message
     * @param cause the original exception that triggered this exception
     */
    public ParameterRetrievalException(String message, Throwable cause) {
      super(message, cause);
    }

  }

}
