package org.folio.rest.camunda.delegate;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.workflow.dto.Request;
import org.folio.rest.workflow.model.FolioRequestTask;
import org.folio.spring.web.service.HttpService;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * A delegate for performing already logged in FOLIO HTTP requests.
 *
 * For logging into FOLIO, use the RequestDelegate instead.
 */
@Service
@Scope("prototype")
public class FolioRequestDelegate extends RequestDelegate {

  public FolioRequestDelegate(HttpService httpService) {
    super(httpService);
  }

  @Override
  public Class<?> fromTask() {
    return FolioRequestTask.class;
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   * @param name      The delegate name.
   * @param id        The delegate ID.
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {

    final long startTime = determineStartTime(execution);

    final Map<String, Object> inputs = getInputs(execution);
    final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    final Request requestValue = objectMapper.readValue(this.request.getValue(execution).toString(), Request.class);
    final Boolean sendEmptyBody = requestValue.getSendEmptyBody();

    final StringTemplateLoader loader = new StringTemplateLoader();
    cfg.setTemplateLoader(loader);

    final String body = performExecuteBuildBody(requestValue.getBodyTemplate(), loader, cfg, inputs);
    final String url = performExecuteBuildUrl(requestValue.getUrl(), loader, cfg, inputs);

    final HttpMethod method = HttpMethod.valueOf(requestValue.getMethod().toString());
    final String accept = requestValue.getAccept();
    final String contentType = requestValue.getContentType();

    final String tenant = execution.getTenantId();
    final Object token = execution.getVariable("X-Okapi-Token");

    getLogger().info("url: {}", url);
    getLogger().debug("method: {}", method);
    getLogger().debug("sendEmptyBody: {}", sendEmptyBody);

    getLogger().debug("accept: {}", accept);
    getLogger().debug("content-type: {}", contentType);
    getLogger().debug("tenant: {}", tenant);

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", accept);
    headers.add("Content-Type", contentType);
    headers.add("X-Okapi-Tenant", tenant);
    headers.add("X-Okapi-Url", okapiUrl);

    if (token != null) {
      headers.add("X-Okapi-Token", token.toString());
    }

    final HttpEntity<Object> entity = shouldSendBody(body, sendEmptyBody, method)
      ? new HttpEntity<>(body, headers)
      : new HttpEntity<>(headers);

    final ResponseEntity<Object> response = httpService.exchange(url, method, entity, Object.class);

    setOutput(execution, response.getBody());

    getHeaderOutputVariables(execution)
      .forEach(headerOutputVariable -> performExecuteHeaderOutputVariables(execution, headerOutputVariable, response));

    determineEndTime(execution, startTime);
  }

}
