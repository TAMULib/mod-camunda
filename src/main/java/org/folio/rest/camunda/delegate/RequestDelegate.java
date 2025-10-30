package org.folio.rest.camunda.delegate;

import static org.camunda.spin.Spin.JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    Request requestValue = objectMapper.readValue(this.request.getValue(execution).toString(), Request.class);

    Map<String, Object> inputs = getInputs(execution);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("url", requestValue.getUrl());
    stringLoader.putTemplate("body", requestValue.getBodyTemplate());
    cfg.setTemplateLoader(stringLoader);

    String url = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("url"), inputs);
    String body = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("body"), inputs);

    HttpMethod method = HttpMethod.valueOf(requestValue.getMethod().toString());
    String accept = requestValue.getAccept();
    String contentType = requestValue.getContentType();

    String tenant = execution.getTenantId();

    Optional<Object> token = Optional.ofNullable(execution.getVariable("X-Okapi-Token"));

    getLogger().info("url: {}", url);
    getLogger().debug("method: {}", method);

    getLogger().debug("accept: {}", accept);
    getLogger().debug("content-type: {}", contentType);
    getLogger().debug("tenant: {}", tenant);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", accept);
    headers.add("Content-Type", contentType);
    headers.add("X-Okapi-Tenant", tenant);
    headers.add("X-Okapi-Url", okapiUrl);

    if (token.isPresent()) {
      headers.add("X-Okapi-Token", token.get().toString());
    }

    HttpEntity<Object> entity = new HttpEntity<>(body, headers);
    ResponseEntity<Object> response = httpService.exchange(url, method, entity, Object.class);

    setOutput(execution, response.getBody());

    getHeaderOutputVariables(execution).forEach(headerOutputVariable -> {
      if (headerOutputVariable.getKey() != null) {
        VariableType type = headerOutputVariable.getType();
        String key = headerOutputVariable.getKey();

        if (response.getHeaders().containsKey(key)) {
          if (type != null) {
            Object value = null;

            if (headerOutputVariable.getAsArray()) {
              List<String> valueList = new ArrayList<>();

              if (response.getHeaders().containsKey(key)) {
                valueList.addAll(response.getHeaders().get(key));
              }

              value = spinValue(headerOutputVariable, valueList);
            } else {
              value = spinValue(headerOutputVariable, response.getHeaders().getFirst(key));
            }

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
    });

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
