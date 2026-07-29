package org.folio.rest.camunda.delegate;

import static org.camunda.spin.Spin.JSON;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.TRACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.camunda.exception.DelegateSpinFailure;
import org.folio.rest.workflow.dto.Request;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.RequestTask;
import org.folio.spring.web.service.HttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
@Scope("prototype")
public class RequestDelegate extends AbstractWorkflowIODelegate {

  @Value("${okapi.url}")
  private String okapiUrl;

  private HttpService httpService;

  private Expression request;

  private Expression headerOutputVariables;

  public RequestDelegate(HttpService httpService) {
    this.httpService = httpService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    final Request requestValue = objectMapper.readValue(this.request.getValue(execution).toString(), Request.class);

    final Map<String, Object> inputs = getInputs(execution);
    final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    final String bodyTemplate = requestValue.getBodyTemplate();
    final Boolean sendEmptyBody = requestValue.getSendEmptyBody();

    final StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("url", requestValue.getUrl());

    cfg.setTemplateLoader(stringLoader);

    String body = null;

    if (bodyTemplate != null) {
      stringLoader.putTemplate("body", requestValue.getBodyTemplate());

      body = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("body"), inputs);
    }

    final String url = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("url"), inputs);

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

  public void setRequest(Expression request) {
    this.request = request;
  }

  public void setHeaderOutputVariables(Expression headerOutputVariables) {
    this.headerOutputVariables = headerOutputVariables;
  }

  @Override
  public Class<?> fromTask() {
    return RequestTask.class;
  }

  public Set<EmbeddedVariable> getHeaderOutputVariables(DelegateExecution execution) throws JsonProcessingException {
    // @formatter:off
    return objectMapper.readValue(headerOutputVariables.getValue(execution).toString(),
        new TypeReference<Set<EmbeddedVariable>>() {});
    // @formatter:on
  }

  /**
   * Perform the delegate execution relating to header output variables.
   *
   * @param execution            The delegate execution data.
   * @param headerOutputVariable The embedded variable.
   */
  private void performExecuteHeaderOutputVariables(
    final DelegateExecution execution, final EmbeddedVariable headerOutputVariable, final ResponseEntity<Object> response
  ) {

    if (headerOutputVariable.getKey() != null) {
      VariableType type = headerOutputVariable.getType();
      String key = headerOutputVariable.getKey();

      if (response.getHeaders().containsKey(key)) {
        if (type != null) {
          final Object value = performExecuteHeaderOutputVariablesSpin(headerOutputVariable, response.getHeaders(), key);

          switch (type) {
            case LOCAL:
              execution.setVariableLocal(key, value);
              break;
            case PROCESS:
              execution.setVariable(key, value);
              break;
            default:
              break;
          }
        } else {
          getLogger().warn("Variable type not present for {}.", key);
        }
      } else {
        getLogger().warn("Header output not present for {}.", key);
      }
    } else {
      getLogger().warn("Header output key is not found in the response.");
    }
  }

  /**
   * Perform the delegate execution relating to header output variables for spin.
   *
   * @param headerOutputVariable The embedded variable.
   * @param headers              The HTTP headers.
   * @param key                  The key representing the array index.
   *
   * @return The value, after "spinning".
   */
  private Object performExecuteHeaderOutputVariablesSpin(final EmbeddedVariable headerOutputVariable, final HttpHeaders headers, final String key) {

    if (Boolean.TRUE.equals(headerOutputVariable.getAsArray())) {
      final List<String> valueList = new ArrayList<>();

      if (headers.containsKey(key)) {
        valueList.addAll(headers.get(key));
      }

      return spinValue(headerOutputVariable, valueList);
    }

    return spinValue(headerOutputVariable, headers.getFirst(key));
  }

  /**
   * Determine if HTML body section should be sent or not.
   *
   * The body string, the HTTP method, and the sendEmptyBody are all used to determine whether or not the body should be sent.
   * If this returns true, then the body should be sent regardless of whether or not it is NULL or some string.
   *
   * The HTTP TRACE must never send an HTTP body section.
   *
   * @param body          The body string.
   * @param sendEmptyBody Whether or not to send an empty body.
   * @param method        The HTTP Method.
   *
   * @return TRUE on send body and FALSE otherwise.
   */
  private boolean shouldSendBody(final String body, final Boolean sendEmptyBody, final HttpMethod method) {

    if (TRACE.equals(method)) return false;

    if ((body == null || body.isEmpty()) && (DELETE.equals(method) || GET.equals(method) || HEAD.equals(method))) {
      return Boolean.TRUE.equals(sendEmptyBody);
    }

    return true;
  }


  /**
   * Conditional spin the value if spin is enabled.
   *
   * @param variable The embedded variable.
   * @param value The variable value to spin.
   *
   * @return Object the spin object value or the original value.
   *
   * @throws DelegateSpinFailure On spin error.
   */
  private Object spinValue(EmbeddedVariable variable, Object value) throws DelegateSpinFailure {
    try {
      return variable.getSpin() ? JSON(objectMapper.writeValueAsString(value)) : value;
    } catch (JsonProcessingException e) {
      throw new DelegateSpinFailure(variable.getKey(), RequestDelegate.class.getName(), e);
    }
  }

}
