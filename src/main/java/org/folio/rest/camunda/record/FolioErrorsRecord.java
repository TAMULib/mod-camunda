package org.folio.rest.camunda.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Provide a basic structure for a FOLIO record that is intended to be used to represent a generic error JSON from FOLIO.
 *
 * @param errors The list of errors.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FolioErrorsRecord(

  @JsonProperty("errors")
  List<FolioErrorRecord> errors

) { }
