package org.folio.rest.camunda.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

/**
 * Provides a basic FOLIO token record, be it an access token, a refresh token, or any other token.
 *
 * @param expire The expiration time stamp of the token.
 * @param token  The FOLIO token.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FolioTokenRecord(

  @JsonProperty("expire")
  ZonedDateTime expire,

  @JsonProperty("token")
  String token

) { }
