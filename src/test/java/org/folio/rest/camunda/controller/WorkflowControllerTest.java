package org.folio.rest.camunda.controller;

import static org.folio.spring.test.mock.MockMvcConstant.APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.APP_RAML;
import static org.folio.spring.test.mock.MockMvcConstant.APP_SCHEMA;
import static org.folio.spring.test.mock.MockMvcConstant.APP_STAR;
import static org.folio.spring.test.mock.MockMvcConstant.APP_STREAM;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.MT_APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.NO_PARAM;
import static org.folio.spring.test.mock.MockMvcConstant.NULL_STR;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD_NO_URL;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD_TENANT;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD_TOKEN;
import static org.folio.spring.test.mock.MockMvcConstant.PLAIN_BODY;
import static org.folio.spring.test.mock.MockMvcConstant.STAR;
import static org.folio.spring.test.mock.MockMvcConstant.TEXT_PLAIN;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.folio.spring.test.mock.MockMvcReflection.DELETE;
import static org.folio.spring.test.mock.MockMvcReflection.GET;
import static org.folio.spring.test.mock.MockMvcReflection.PATCH;
import static org.folio.spring.test.mock.MockMvcReflection.PUT;
import static org.folio.spring.test.mock.MockMvcRequest.appendBody;
import static org.folio.spring.test.mock.MockMvcRequest.appendHeaders;
import static org.folio.spring.test.mock.MockMvcRequest.appendParameters;
import static org.folio.spring.test.mock.MockMvcRequest.buildArguments1;
import static org.folio.spring.test.mock.MockMvcRequest.buildArguments2;
import static org.folio.spring.test.mock.MockMvcRequest.invokeRequestBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Method;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.folio.rest.camunda.service.CamundaApiService;
import org.folio.rest.workflow.model.Setup;
import org.folio.rest.workflow.model.Workflow;
import org.folio.spring.tenant.properties.TenantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;

@WebMvcTest(WorkflowController.class)
@ExtendWith(MockitoExtension.class)
class WorkflowControllerTest {

  private static final String PATH = "/workflow-engine/workflows";

  private static final String PATH_ACTIVATE = PATH + "/activate";

  private static final String PATH_DEACTIVATE = PATH + "/deactivate";

  private MockMvc mvc;

  @Autowired
  protected ObjectMapper mapper;

  @Autowired
  private WorkflowController workflowController;

  @MockitoBean
  private CamundaApiService camundaApiService;

  @MockitoBean
  private TenantProperties tenantProperties;

  @BeforeEach
  void beforeEach() {
    mvc = MockMvcBuilders.standaloneSetup(workflowController).build();

    // Prevent problems where the object mapper randomly switches time zone thereby causing string comparisons to fail.
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @ParameterizedTest
  @MethodSource("provideHeadersBodyStatusForActivateDeactivateWorkflow")
  void activateWorkflowTest(HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    ObjectNode objectNode = mapper.createObjectNode();
    objectNode.put(KEY, VALUE);

    Workflow workflow = new Workflow();
    workflow.setId("id");
    workflow.setName("name");
    workflow.setDescription("description");
    workflow.setVersionTag("versionTag");
    workflow.setHistoryTimeToLive(90);
    workflow.setDeploymentId("deploymentId");
    workflow.setSetup(new Setup());

    lenient().when(camundaApiService.deployWorkflow(any(), any())).thenReturn(workflow);

    MockHttpServletRequestBuilder request = appendHeaders(post(PATH_ACTIVATE), headers, contentType, accept);
    request = appendParameters(request, parameters);

    MvcResult result = mvc.perform(appendBody(request, body))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    if (status == 200) {
      MediaType responseType = MediaType.parseMediaType(result.getResponse().getContentType());
      String workflowJson = mapper.writeValueAsString(workflow);

      assertTrue(mediaType.isCompatibleWith(responseType));

      Workflow responseWorkflow = mapper.readValue(result.getResponse().getContentAsString(), Workflow.class);

      assertEquals(workflowJson, mapper.writeValueAsString(responseWorkflow));
    }
  }

  @ParameterizedTest
  @MethodSource("provideHeadersBodyStatusForActivateDeactivateWorkflow")
  void deactivateWorkflowTest(HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    ObjectNode objectNode = mapper.createObjectNode();
    objectNode.put(KEY, VALUE);

    Workflow workflow = new Workflow();
    workflow.setId("id");
    workflow.setName("name");
    workflow.setDescription("description");
    workflow.setVersionTag("versionTag");
    workflow.setHistoryTimeToLive(90);
    workflow.setDeploymentId("deploymentId");
    workflow.setSetup(new Setup());

    lenient().when(camundaApiService.undeployWorkflow(any())).thenReturn(workflow);

    MockHttpServletRequestBuilder request = appendHeaders(post(PATH_DEACTIVATE), headers, contentType, accept);
    request = appendParameters(request, parameters);

    MvcResult result = mvc.perform(appendBody(request, body))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    if (status == 200) {
      MediaType responseType = MediaType.parseMediaType(result.getResponse().getContentType());
      String workflowJson = mapper.writeValueAsString(workflow);

      assertTrue(mediaType.isCompatibleWith(responseType));

      Workflow responseWorkflow = mapper.readValue(result.getResponse().getContentAsString(), Workflow.class);

      assertEquals(workflowJson, mapper.writeValueAsString(responseWorkflow));
    }
  }

  @ParameterizedTest
  @MethodSource("provideDeleteGetPatchPutForActivateDeactivateWorkflow")
  void activateWorkflowFailsTest(Method method, HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    mvc.perform(invokeRequestBuilder(PATH_ACTIVATE, method, headers, contentType, accept, parameters, body))
      .andDo(log()).andExpect(status().is(status));
  }

  @ParameterizedTest
  @MethodSource("provideDeleteGetPatchPutForActivateDeactivateWorkflow")
  void deactivateWorkflowFailsTest(Method method, HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    mvc.perform(invokeRequestBuilder(PATH_DEACTIVATE, method, headers, contentType, accept, parameters, body))
      .andDo(log()).andExpect(status().is(status));
  }

  /**
   * Helper function for parameterized test providing DELETE, GET, PATCH, and PUT.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Method The (reflection) request method.
   *     - HttpHeaders headers.
   *     - String contentType (Content-Type request).
   *     - String accept (ask for this Content-Type on response).
   *       String mediaType (response Content-Type).
   *     - MultiValueMap<String, String> parameters.
   *     - String body (request body).
   *     - int status (response HTTP status code).
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static Stream<Arguments> provideDeleteGetPatchPutForActivateDeactivateWorkflow() throws NoSuchMethodException, SecurityException {
    String[] contentTypes = { APP_JSON, TEXT_PLAIN, APP_STREAM };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, JSON_OBJECT };
    String[] accepts = { APP_RAML, APP_SCHEMA, APP_JSON, TEXT_PLAIN, APP_STREAM, NULL_STR, APP_STAR, STAR };
    MediaType[] mediaTypes = { MT_APP_JSON };
    Object[] params = { NO_PARAM };

    Stream<Arguments> stream1 = buildArguments2(DELETE, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    Stream<Arguments> stream2 = buildArguments2(GET, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    Stream<Arguments> stream = Stream.concat(stream1, stream2);

    stream1 = buildArguments2(PATCH, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    stream2 = Stream.concat(stream, stream1);

    stream1 = buildArguments2(PUT, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    return Stream.concat(stream2, stream1);
  }

  /**
   * Helper function for parameterized test providing tests with headers, body, and status for activate workflow end point.
   *
   * This is intended to be used for when the correct HTTP method is being used in the request.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - HttpHeaders headers.
   *     - String contentType (Content-Type request).
   *     - String accept (ask for this Content-Type on response).
   *       String mediaType (response Content-Type).
   *     - MultiValueMap<String, String> parameters.
   *     - String body (request body).
   *     - int status (response HTTP status code).
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static Stream<Arguments> provideHeadersBodyStatusForActivateDeactivateWorkflow() throws NoSuchMethodException, SecurityException {
    Stream<Arguments> stream1 = Stream.of(
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   APP_SCHEMA, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   APP_JSON,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   TEXT_PLAIN, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   APP_STREAM, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   NULL_STR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   APP_STAR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD_TENANT, APP_JSON,   STAR,       MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, APP_SCHEMA, MT_APP_JSON, NO_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, APP_JSON,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  415),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, TEXT_PLAIN, MT_APP_JSON, NO_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, APP_STREAM, MT_APP_JSON, NO_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, NULL_STR,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  415),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, STAR,       MT_APP_JSON, NO_PARAM, PLAIN_BODY,  415),
      Arguments.of(OKAPI_HEAD_TENANT, TEXT_PLAIN, APP_STAR,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  415),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, APP_SCHEMA, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, APP_JSON,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 415),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, TEXT_PLAIN, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, APP_STREAM, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, NULL_STR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 415),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, APP_STAR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 415),
      Arguments.of(OKAPI_HEAD_TENANT, APP_STREAM, STAR,       MT_APP_JSON, NO_PARAM, JSON_OBJECT, 415)
    );

    String[] contentTypes = { APP_JSON, TEXT_PLAIN, APP_STREAM };
    String[] contentTypesValid = { APP_JSON };
    String[] contentTypesInvalid = { TEXT_PLAIN, APP_STREAM };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, JSON_OBJECT };
    String[] bodysValid = { JSON_OBJECT };
    String[] bodysInvalid = { PLAIN_BODY, JSON_OBJECT };
    String[] accepts = { APP_JSON, NULL_STR, APP_STAR, STAR };
    String[] acceptsInvalid = { APP_SCHEMA, TEXT_PLAIN, APP_STREAM };
    MediaType[] mediaTypes = { MT_APP_JSON };
    Object[] params = { NO_PARAM };

    Stream<Arguments> stream2 = buildArguments1(OKAPI_HEAD_TOKEN, contentTypes, acceptsInvalid, mediaTypes, params, bodys, 406);
    Stream<Arguments> stream = Stream.concat(stream1, stream2);

    stream1 = buildArguments1(OKAPI_HEAD_TOKEN, contentTypesValid, accepts, mediaTypes, params, bodysValid, 200);
    stream2 = Stream.concat(stream, stream1);

    stream = buildArguments1(OKAPI_HEAD_TOKEN, contentTypesInvalid, accepts, mediaTypes, params, bodysInvalid, 415);
    stream1 = Stream.concat(stream, stream2);

    stream2 = buildArguments1(OKAPI_HEAD_TENANT, contentTypesValid, accepts, mediaTypes, params, bodysValid, 200);
    stream = Stream.concat(stream1, stream2);

    stream1 = buildArguments1(OKAPI_HEAD_TENANT, contentTypesInvalid, accepts, mediaTypes, params, bodysInvalid, 415);
    return Stream.concat(stream, stream1);
  }

}
