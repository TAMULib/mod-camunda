package org.folio.rest.camunda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

/**
 * Provide a structure representing the FOLIO login response.
 *
 * This is intended to be used as a type in the JsonResponse.
 *
 * @param accessTokenExpiration  The access token expiration.
 * @param refreshTokenExpiration The refresh token expiration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FolioLoginResponse(

  @JsonProperty("accessTokenExpiration")
  ZonedDateTime accessTokenExpiration,

  @JsonProperty("refreshTokenExpiration")
  ZonedDateTime refreshTokenExpiration

) { }
