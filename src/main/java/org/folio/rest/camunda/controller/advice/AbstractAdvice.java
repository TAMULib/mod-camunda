package org.folio.rest.camunda.controller.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.spring.web.utility.ErrorUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

abstract class AbstractAdvice extends RequestMappingHandlerMapping {

  /**
   * Get the object mapper
   *
   * @return objectMapper The object mapper.
   */
  protected abstract ObjectMapper getObjectMapper();

  /**
   * Build the error message, with default JSON media type.
   *
   * @param ex The exception.
   * @param code The HTTP Status Code.
   *
   * @return The built error response entity.
   */
  protected ResponseEntity<String>  buildError(Exception ex, HttpStatus code) {
    return buildError(ex, code, MediaType.APPLICATION_JSON);
  }

  /**
   * Build the error message.
   *
   * @param ex The exception.
   * @param code The HTTP Status Code.
   * @param type The media type to use.
   *
   * @return The built error response entity.
   */
  protected ResponseEntity<String>  buildError(Exception ex, HttpStatus code, MediaType type) {
    String message = ex.getMessage();

    logger.error(message, logger.isDebugEnabled() ? ex : null);

    // The exception handler should ideally not throw its own exceptions.
    // Catch the exceptions and report it, then fall back to a plain text error message.
    try {
      message = getObjectMapper().writeValueAsString(ErrorUtility.buildError(ex, code));
    } catch (JsonProcessingException e) {
      logger.error("Mapping error to JSON Object failed.", e);

      type = MediaType.TEXT_PLAIN;
    }

    return ResponseEntity.status(code)
      .contentType(type)
      .body(message);
  }

}
