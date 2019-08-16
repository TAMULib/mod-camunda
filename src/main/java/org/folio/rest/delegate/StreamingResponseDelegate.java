package org.folio.rest.delegate;

import static org.camunda.spin.Spin.JSON;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.service.LoginService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

@Service
@Scope("prototype")
public class StreamingResponseDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private LoginService loginService;

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Value("${okapi.username}")
  private String OKAPI_USERNAME;

  @Value("${okapi.password}")
  private String OKAPI_PASSWORD;

  private Expression streamSource;

  private final WebClient.Builder webClientBuilder;

  public StreamingResponseDelegate(WebClient.Builder webClientBuilder) {
    super();
    this.webClientBuilder = webClientBuilder;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    String sourceBaseUrl = streamSource != null ? streamSource.getValue(execution).toString() : OKAPI_LOCATION;

    WebClient webClient = webClientBuilder.baseUrl(sourceBaseUrl).build();

    String delegateName = execution.getBpmnModelElementInstance().getName();
    log.info(String.format("%s STARTED", delegateName));

    JsonNode payload = (JsonNode) execution.getVariable("payload");
    String extratorId = payload.get("extractorId").asText();

    String tenant = execution.getTenantId();

    FolioLogin newLogin = login(DEFAULT_TENANT, OKAPI_LOCATION, OKAPI_USERNAME, OKAPI_PASSWORD);
    log.info("NEW LOGIN: {}", newLogin);
    String token = newLogin.getxOkapiToken();
    execution.setVariable("okapiToken", token);

    log.info("START REQUEST");

    String fluxId = streamService.setFlux(
      webClient
        .get()
        .uri("%s/extractors/{id}/run", extratorId)
        .header("X-Okapi-Tenant", tenant)
        .header("X-Okapi-Token", token)
        .accept(MediaType.APPLICATION_STREAM_JSON)
        .retrieve()
        .bodyToFlux(String.class)
    );

    execution.setVariable("primaryStreamId", fluxId);

    log.info("STREAM DELEGATE FINISHED");
  }

  private FolioLogin login(String tenant, String baseUrl, String username, String password) {

    OkapiRequest loginOkapiRequest = new OkapiRequest();
    loginOkapiRequest.setTenant(tenant);
    loginOkapiRequest.setRequestUrl(String.format("%s/authn/login", baseUrl));
    loginOkapiRequest.setRequestMethod("POST");
    loginOkapiRequest.setRequestContentType("application/json");
    loginOkapiRequest.setResponseBodyName("loginResponseBody");
    loginOkapiRequest.setResponseHeaderName("loginResponseHeader");
    loginOkapiRequest.setResponseStatusName("loginResponseStatus");

    // A bit redundant, may want to create a login payload model, or eventually handle this better
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", username);
    jsonObject.put("password", password);

    SpinJsonNode loginJsonNode = JSON(jsonObject.toString());

    loginOkapiRequest.setRequestPayload(loginJsonNode);
    FolioLogin newLogin = loginService.folioLogin(loginOkapiRequest);
    newLogin.setUsername(username);
    return newLogin;
  }

  public void setStreamSource(Expression streamSource) {
    this.streamSource = streamSource;
  }

}
