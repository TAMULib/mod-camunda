package org.folio.rest.camunda.utility;

import static org.folio.rest.camunda.utility.MappingParametersType.ALTERNATIVE_TITLE_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.CALL_NUMBER_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.CLASSIFICATION_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.CONTRIBUTOR_NAME_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.CONTRIBUTOR_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.ELECTRONIC_ACCESS_RELATIONSHIPS;
import static org.folio.rest.camunda.utility.MappingParametersType.HOLDINGS_NOTE_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.HOLDINGS_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.IDENTIFIER_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.ILL_POLICIES;
import static org.folio.rest.camunda.utility.MappingParametersType.INSTANCE_FORMATS;
import static org.folio.rest.camunda.utility.MappingParametersType.INSTANCE_NOTE_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.INSTANCE_RELATIONSHIP_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.INSTANCE_STATUSES;
import static org.folio.rest.camunda.utility.MappingParametersType.INSTANCE_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.ISSUANCE_MODES;
import static org.folio.rest.camunda.utility.MappingParametersType.ITEM_DAMAGED_STATUSES;
import static org.folio.rest.camunda.utility.MappingParametersType.ITEM_NOTE_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.LOAN_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.LOCATIONS;
import static org.folio.rest.camunda.utility.MappingParametersType.MARC_FIELD_PROTECTION_SETTINGS;
import static org.folio.rest.camunda.utility.MappingParametersType.MATERIAL_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.NATURE_OF_CONTENT_TERMS;
import static org.folio.rest.camunda.utility.MappingParametersType.STATISTICAL_CODES;
import static org.folio.rest.camunda.utility.MappingParametersType.STATISTICAL_CODE_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersType.SUBJECT_SOURCES;
import static org.folio.rest.camunda.utility.MappingParametersType.SUBJECT_TYPES;
import static org.folio.rest.camunda.utility.MappingParametersUtility.LIMIT;
import static org.folio.rest.camunda.utility.MappingParametersUtility.MAPPING_RULES_PATH;
import static org.folio.rest.camunda.utility.TestUtility.i;
import static org.folio.rest.camunda.utility.TestUtility.om;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.folio.Alternativetitletypes;
import org.folio.Callnumbertypes;
import org.folio.Classificationtypes;
import org.folio.Contributornametypes;
import org.folio.Contributortypes;
import org.folio.Electronicaccessrelationships;
import org.folio.Holdingsnotetypes;
import org.folio.Holdingstypes;
import org.folio.Identifiertypes;
import org.folio.Illpolicies;
import org.folio.Instanceformats;
import org.folio.Instancenotetypes;
import org.folio.Instancerelationshiptypes;
import org.folio.Instancestatuses;
import org.folio.Instancetypes;
import org.folio.Issuancemodes;
import org.folio.Itemdamagedstatuses;
import org.folio.Itemnotetypes;
import org.folio.Loantypes;
import org.folio.Locations;
import org.folio.MarcFieldProtectionSettingsCollection;
import org.folio.Materialtypes;
import org.folio.Natureofcontentterms;
import org.folio.Statisticalcodes;
import org.folio.Statisticalcodetypes;
import org.folio.SubjectSources;
import org.folio.SubjectTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class MappingUtilityTest {

  private final static String OKAPI_URL = "http://localhost:9130";

  @Spy
  private OkapiRestTemplate mockRestTemplate;

  @BeforeEach
  void beforeEach() throws RestClientException, IOException {
    mockJsonResponse("rules.json", MAPPING_RULES_PATH, JsonNode.class);

    mockJsonResponse("Alternativetitletypes.json", ALTERNATIVE_TITLE_TYPES.getPath(LIMIT), Alternativetitletypes.class);
    mockJsonResponse("Callnumbertypes.json", CALL_NUMBER_TYPES.getPath(LIMIT), Callnumbertypes.class);
    mockJsonResponse("Classificationtypes.json", CLASSIFICATION_TYPES.getPath(LIMIT), Classificationtypes.class);
    mockJsonResponse("Contributornametypes.json", CONTRIBUTOR_NAME_TYPES.getPath(LIMIT), Contributornametypes.class);
    mockJsonResponse("Contributortypes.json", CONTRIBUTOR_TYPES.getPath(LIMIT), Contributortypes.class);
    mockJsonResponse("Electronicaccessrelationships.json", ELECTRONIC_ACCESS_RELATIONSHIPS.getPath(LIMIT), Electronicaccessrelationships.class);
    mockJsonResponse("Holdingsnotetypes.json", HOLDINGS_NOTE_TYPES.getPath(LIMIT), Holdingsnotetypes.class);
    mockJsonResponse("Holdingstypes.json", HOLDINGS_TYPES.getPath(LIMIT), Holdingstypes.class);
    mockJsonResponse("Identifiertypes.json", IDENTIFIER_TYPES.getPath(LIMIT), Identifiertypes.class);
    mockJsonResponse("Illpolicies.json", ILL_POLICIES.getPath(LIMIT), Illpolicies.class);
    mockJsonResponse("Instanceformats.json", INSTANCE_FORMATS.getPath(LIMIT), Instanceformats.class);
    mockJsonResponse("Instancenotetypes.json", INSTANCE_NOTE_TYPES.getPath(LIMIT), Instancenotetypes.class);
    mockJsonResponse("Instancerelationshiptypes.json", INSTANCE_RELATIONSHIP_TYPES.getPath(LIMIT), Instancerelationshiptypes.class);
    mockJsonResponse("Instancestatuses.json", INSTANCE_STATUSES.getPath(LIMIT), Instancestatuses.class);
    mockJsonResponse("Instancetypes.json", INSTANCE_TYPES.getPath(LIMIT), Instancetypes.class);
    mockJsonResponse("Issuancemodes.json", ISSUANCE_MODES.getPath(LIMIT), Issuancemodes.class);
    mockJsonResponse("Itemdamagedstatuses.json", ITEM_DAMAGED_STATUSES.getPath(LIMIT), Itemdamagedstatuses.class);
    mockJsonResponse("Itemnotetypes.json", ITEM_NOTE_TYPES.getPath(LIMIT), Itemnotetypes.class);
    mockJsonResponse("Loantypes.json", LOAN_TYPES.getPath(LIMIT), Loantypes.class);
    mockJsonResponse("Locations.json", LOCATIONS.getPath(LIMIT), Locations.class);
    mockJsonResponse("MarcFieldProtectionSettingsCollection.json", MARC_FIELD_PROTECTION_SETTINGS.getPath(LIMIT), MarcFieldProtectionSettingsCollection.class);
    mockJsonResponse("Materialtypes.json", MATERIAL_TYPES.getPath(LIMIT), Materialtypes.class);
    mockJsonResponse("Natureofcontentterms.json", NATURE_OF_CONTENT_TERMS.getPath(LIMIT), Natureofcontentterms.class);
    mockJsonResponse("Statisticalcodes.json", STATISTICAL_CODES.getPath(LIMIT), Statisticalcodes.class);
    mockJsonResponse("Statisticalcodetypes.json", STATISTICAL_CODE_TYPES.getPath(LIMIT), Statisticalcodetypes.class);
    mockJsonResponse("Subjectsources.json", SUBJECT_SOURCES.getPath(LIMIT), SubjectSources.class);
    mockJsonResponse("Subjecttypes.json", SUBJECT_TYPES.getPath(LIMIT), SubjectTypes.class);

    MappingUtility.restTemplate = mockRestTemplate;
  }

  @ParameterizedTest
  @MethodSource("testMapRecordToInsanceStream")
  void testMapRecordToInsance(Parameters<String, String> data) throws JsonProcessingException {
    String marcJson = data.input;
    String okapiUrl = OKAPI_URL;
    String tenant = "diku";
    String token = "token";

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(),
          () -> MappingUtility.mapRecordToInsance(marcJson, okapiUrl, tenant, token));
    } else {
      ObjectNode expected = (ObjectNode) om(data.expected);
      expected.remove("id");
      ObjectNode actual = (ObjectNode) om(MappingUtility.mapRecordToInsance(marcJson, okapiUrl, tenant, token));
      actual.remove("id");

      assertEquals(expected, actual);
    }
  }

  /**
   * Stream parameters for testing mapRecordToInsance.
   *
   * @return
   *         The test method parameters:
   *         - input of type String (MARC JSON)
   *         - expected output of type String (JSON FOLIO instance).
   *         - exception expected to be thrown
   */
  static Stream<Parameters<String, String>> testMapRecordToInsanceStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new IllegalArgumentException()),
        Parameters.of("", null, new IllegalArgumentException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json"), i("/folio/instances/54-008008027.json")),
        Parameters.of(i("/marc4j/54-56-008008027-1.mrc.json"), i("/folio/instances/55-008008027.json")),
        Parameters.of(i("/marc4j/54-56-008008027-2.mrc.json"), i("/folio/instances/56-008008027.json")),
        Parameters.of(i("/marc4j/54-56-008008027-3.mrc.json"), i("/folio/instances/57-008008027.json")),
        Parameters.of(i("/marc4j/54-56-008008027-4.mrc.json"), i("/folio/instances/58-008008027.json")));
  }

  /**
   * Mock the JSON response in a lenient manner.
   *
   * This is intended to simplify the mock code.
   *
   * @param name
   *              The name of the JSON file from the FOLIO settings path.
   * @param path
   *              The Okapi path for this JSON.
   * @param clazz
   *              The class type the JSON file is being returned as.
   *
   * @throws IOException         On I/O error.
   * @throws RestClientException On Rest Client error.
   */
  private void mockJsonResponse(String name, String path, Class<?> clazz) throws RestClientException, IOException {
    lenient()
        .doReturn(i("/folio/settings/" + name, clazz))
        .when(mockRestTemplate).getForEntity(eq(path), eq(clazz));
  }

}
