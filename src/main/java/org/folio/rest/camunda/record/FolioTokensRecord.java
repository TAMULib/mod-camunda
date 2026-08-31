package org.folio.rest.camunda.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides a collection of FOLIO token records.
 *
 * @param access  The access token.
 * @param refresh The refresh token.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FolioTokensRecord(

  @JsonProperty("access")
  FolioTokenRecord access,

  @JsonProperty("refresh")
  FolioTokenRecord refresh

) { }
