package org.folio.rest.camunda.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provide a structure for use with a FOLIO login request body.
 *
 * @param password The password of the user.
 * @param username The name of the user.
 */
public record FolioLoginRequest(

  @JsonProperty("password")
  String password,

  @JsonProperty("username")
  String username

) { }
