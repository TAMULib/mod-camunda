package org.folio.rest.delegate;

import static org.camunda.spin.Spin.JSON;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FolioLoginDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private LoginService loginService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Folio Login Delegate");

    OkapiRequest okapiRequest = new OkapiRequest();
    okapiRequest.setUrl("http://localhost:9130/authn/login");
    okapiRequest.setRequestMethod("POST");
    okapiRequest.setRequestContentType("application/json");
    okapiRequest.setResponseBodyName("loginResponseBody");
    okapiRequest.setResponseHeaderName("loginResponseHeader");
    okapiRequest.setResponseStatusName("loginResponseStatus");
    okapiRequest.setTenant("diku");

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", "diku_admin");
    jsonObject.put("password", "admin");

    okapiRequest.setPayload(jsonObject);
    log.info("json: {}", jsonObject.toString());

    FolioLogin newLogin = loginService.folioLogin(okapiRequest);
    newLogin.setUsername("diku_admin");
    log.info("NEW LOGIN: {}", newLogin);

    String json = JSON(newLogin).toString();

    execution.setVariable("folioLogin", newLogin);
    execution.setVariable("loginJson", json);
  }

}
