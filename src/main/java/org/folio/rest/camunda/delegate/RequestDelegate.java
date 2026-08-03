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
import freemarker.template.TemplateException;
import java.io.IOException;
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

/**
 * A delegate for performing already logged in FOLIO HTTP requests.
 *
 * For FOLIO related requests other than logging, use the FolioRequestDelegate instead.
 */
@Service
@Scope("prototype")
public class RequestDelegate extends AbstractWorkflowIODelegate {

  @Value("${okapi.url}")
  protected String okapiUrl;

  protected HttpService httpService;

  protected Expression request;

  protected Expression headerOutputVariables;

  public RequestDelegate(HttpService httpService) {
    this.httpService = httpService;
  }

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

    getLogger().info("url: {}", url);
    getLogger().debug("method: {}", method);
    getLogger().debug("sendEmptyBody: {}", sendEmptyBody);

    getLogger().debug("accept: {}", accept);
    getLogger().debug("content-type: {}", contentType);

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", accept);
    headers.add("Content-Type", contentType);

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
   * Build the body from the body template.
   *
   * @param template The body template.
   * @param loader   The string loader.
   * @param cfg      The configuration.
   * @param inputs   The inputs.
   *
   * @return The built body or NULL if there is no template.
   *
   * @throws IOException       On error.
   * @throws TemplateException On error.
   */
  protected String performExecuteBuildBody(final String template, final StringTemplateLoader loader, final Configuration cfg, final Map<String, Object> inputs)
      throws IOException, TemplateException {

    if (template == null) {
      return null;
    }

    loader.putTemplate("body", template);

    return FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("body"), inputs);
  }

  /**
   * Build the body from the URL.
   *
   * @param template The URL.
   * @param loader   The string loader.
   * @param cfg      The configuration.
   * @param inputs   The inputs.
   *
   * @return The built URL or NULL if there is no URL.
   *
   * @throws IOException       On error.
   * @throws TemplateException On error.
   */
  protected String performExecuteBuildUrl(final String url, final StringTemplateLoader loader, final Configuration cfg, final Map<String, Object> inputs)
      throws IOException, TemplateException {

    if (url == null) {
      return null;
    }

    loader.putTemplate("url", url);

    return FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("url"), inputs);
  }

  /**
   * Perform the delegate execution relating to header output variables.
   *
   * @param execution            The delegate execution data.
   * @param headerOutputVariable The embedded variable.
   */
  protected void performExecuteHeaderOutputVariables(
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
  protected Object performExecuteHeaderOutputVariablesSpin(final EmbeddedVariable headerOutputVariable, final HttpHeaders headers, final String key) {

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
  protected boolean shouldSendBody(final String body, final Boolean sendEmptyBody, final HttpMethod method) {

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
  protected Object spinValue(EmbeddedVariable variable, Object value) throws DelegateSpinFailure {
    try {
      return variable.getSpin() ? JSON(objectMapper.writeValueAsString(value)) : value;
    } catch (JsonProcessingException e) {
      throw new DelegateSpinFailure(variable.getKey(), RequestDelegate.class.getName(), e);
    }
  }

}
