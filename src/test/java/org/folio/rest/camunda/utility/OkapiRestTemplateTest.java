package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.folio.rest.camunda.config.CamundaTenantInit;
import org.folio.rest.camunda.config.TokenConfig;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.service.SqlTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.thymeleaf.spring6.SpringTemplateEngine;

@SpringBootTest(
  classes = {
    CamundaTenantInit.class,
    SpringTemplateEngine.class,
    SqlTemplateService.class,
    TenantProperties.class
  },
  webEnvironment = WebEnvironment.MOCK
)
@ExtendWith(MockitoExtension.class)
class OkapiRestTemplateTest {

  @Spy
  OkapiRestTemplate okapiRestTemplate;

  @Test
  void testBuild() {
    OkapiRestTemplate restTemplate = new OkapiRestTemplate();
    assertNotNull(restTemplate);
  }

  @Test
  void testAt() {
    String okapiUrl = "http:://localhost:9130";

    OkapiRestTemplate updated = okapiRestTemplate.at(okapiUrl);

    verify(updated, times(1)).setUriTemplateHandler(any(DefaultUriBuilderFactory.class));

    assertTrue(((DefaultUriBuilderFactory) updated.getUriTemplateHandler()).hasBaseUri());
  }

  @Test
  void testWith() throws IOException {
    String tenant = "diku";
    String token = "token";

    OkapiRestTemplate updated = okapiRestTemplate.with(tenant, token);

    verify(updated, times(1)).setInterceptors(any(List.class));

    List<ClientHttpRequestInterceptor> interceptor = updated.getInterceptors();

    assertNotNull(interceptor);
    assertEquals(1, interceptor.size());

    HttpRequest request = mock(HttpRequest.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
    ClientHttpResponse response = mock(ClientHttpResponse.class);

    byte[] body = new byte[0];

    when(execution.execute(request, body)).thenReturn(response);
    when(request.getHeaders()).thenReturn(headers);

    interceptor.get(0).intercept(request, body, execution);

    verify(headers).set(CamundaTenantInit.getHeaderName(), tenant);
    verify(headers).set(TokenConfig.getTokenHeaderName(), token);

    verify(headers).setAccept(Arrays.asList(APPLICATION_JSON, TEXT_PLAIN));
    verify(headers).setContentType(APPLICATION_JSON);
  }

}
