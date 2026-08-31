package org.folio.rest.camunda.client;

import java.net.URI;
import java.util.Map;
import org.folio.rest.camunda.request.FolioLoginRequest;
import org.folio.rest.camunda.response.FolioLoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Client for making requests to the FOLIO host.
 */
@Component
@HttpExchange(
  accept = "application/json",
  contentType = "application/json"
)
public interface FolioClient {

  /**
   * HTTP POST log in request to the FOLIO host.
   *
   * @param url               The full URL path.
   * @param headers           The HTTP headers.
   * @param folioLoginRequest The login request body.
   *
   * @return The FOLIO response.
   */
  @PostExchange
  ResponseEntity<FolioLoginResponse> postLogin(

    URI url,

    @RequestHeader
    Map<String, String> headers,

    @RequestBody
    FolioLoginRequest folioLoginRequest
  );

}
