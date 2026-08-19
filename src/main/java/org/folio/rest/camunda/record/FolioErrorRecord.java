package org.folio.rest.camunda.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Provide a basic structure for a FOLIO record that is intended to be used to represent a generic error JSON from FOLIO.
 *
 * @param code       The error code.
 * @param parameters Additional key/value parameters (values are stored as string and might need additional conversion).
 * @param message    The error message.
 * @param type       The error type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FolioErrorRecord(

  @JsonProperty("code")
  String code,

  @JsonProperty("parameters")
  List<Map<String, String>> parameters,

  @JsonProperty("message")
  String message,

  @JsonProperty("type")
  String type

) { }
