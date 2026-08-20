package org.folio.rest.camunda.utility;

import static org.folio.rest.camunda.cache.FolioTokenCache.GATEWAY_URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.Instance;
import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.camunda.cache.FolioTokenCache;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping between different data formats and transforming
 * records in the FOLIO library management system context.
 *
 * This class provides static methods for converting CSV to JSON and mapping
 * MARC records to FOLIO Instance records using predefined mapping rules and
 * parameters.
 */
@Component
public class MappingUtility {

  /** Error message for null or empty CSV input. */
  private static final String ILLEGAL_CSV_ARGUMENT_MESSAGE = "CSV cannot be null or empty";

  /** Error message for null or empty MARC JSON input. */
  private static final String ILLEGAL_MARC_JSON_ARGUMENT_MESSAGE = "MARC JSON record cannot be null or empty";

  /** Error message for null delegate execution input. */
  private static final String ILLEGAL_EXECUTION_ARGUMENT_MESSAGE = "Delegate execution cannot be null";

  /** Mapper for converting MARC records to FOLIO Instance records. */
  private static final MarcToInstanceMapper marcToInstanceMapper = new MarcToInstanceMapper();

  /** Jackson ObjectMapper for JSON serialization and deserialization. */
  private static final ObjectMapper objectMapper = JsonMapper.builder()
    .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
    .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES)
    .disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .addModule(new JavaTimeModule())
    .build();

  /** Rest template for making Okapi-based REST calls. */
  static OkapiRestTemplate restTemplate = new OkapiRestTemplate();

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private MappingUtility() {

    // Should do nothing.
  }

  /**
   * Converts a CSV string to a JSON array representation.
   *
   * <p>
   * This method reads a CSV with headers and transforms it into a JSON array
   * where each element is an object representing a row from the CSV.
   * </p>
   *
   * @param csv The input CSV string to be converted
   * @return A JSON string representing the CSV data as an array of objects
   * @throws IOException              If there's an error reading or parsing the
   *                                  CSV
   * @throws IllegalArgumentException If the CSV input is null or empty
   */
  public static String mapCsvToJson(String csv) throws IOException {
    if (StringUtils.isEmpty(csv)) {
      throw new IllegalArgumentException(ILLEGAL_CSV_ARGUMENT_MESSAGE);
    }
    CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
    CsvMapper csvMapper = new CsvMapper();
    MappingIterator<Map<String, String>> mappingIterator = csvMapper.reader()
      .forType(Map.class)
      .with(csvSchema)
      .readValues(csv);

    return objectMapper.writeValueAsString(mappingIterator.readAll());
  }

  /**
   * Maps a MARC record to a FOLIO Instance record using Okapi services.
   *
   * <p>
   * This method converts a MARC JSON record to a FOLIO Instance record by:
   * </p>
   * <ol>
   * <li>Fetching mapping rules from Okapi</li>
   * <li>Retrieving mapping parameters</li>
   * <li>Applying the MARC to Instance mapping</li>
   * </ol>
   *
   * @param marcJson The MARC record in JSON format.
   * @param okapiUrl The base URL for OKAPI services.
   * @param tenant   The FOLIO tenant identifier.
   * @param token    (optional) Authentication token for OKAPI services. If NULL then use the currently active one.
   *
   * @return A JSON string representation of the mapped FOLIO Instance.
   *
   * @throws JsonProcessingException On error. 
   *
   * @throws IllegalArgumentException If any of the input parameters are null or empty.
   */
  public static String mapRecordToInstance(String marcJson, final DelegateExecution execution) throws JsonProcessingException {

    if (StringUtils.isEmpty(marcJson)) {
      throw new IllegalArgumentException(ILLEGAL_MARC_JSON_ARGUMENT_MESSAGE);
    }

    if (execution == null) {
      throw new IllegalArgumentException(ILLEGAL_EXECUTION_ARGUMENT_MESSAGE);
    }

    final FolioTokenCache folioTokenCache = getFolioTokenCache();

    final String tenant = execution.getTenantId();
    final String gatewayUrl = (String) execution.getVariable(GATEWAY_URL);

    final String accessToken = folioTokenCache.verifyTokens(execution);

    return mapRecordToInstance(restTemplate.at(gatewayUrl).with(tenant, accessToken), marcJson);
  }

  /**
   * Internal method to map a MARC record to a FOLIO Instance using a pre-configured rest template.
   *
   * @param restTemplate The configured OkapiRestTemplate.
   * @param marcJson     The MARC record in JSON format (must not be null).
   *
   * @return A JSON string representation of the mapped FOLIO Instance
   *
   * @throws JsonProcessingException On error.
   */
  private static String mapRecordToInstance(OkapiRestTemplate restTemplate, @NonNull String marcJson) throws JsonProcessingException {

    JsonObject parsedRecord = new JsonObject(marcJson);
    JsonObject mappingRulesObject = MappingParametersUtility.fetchRules(restTemplate);
    JsonNode mappingRulesNode = objectMapper.valueToTree(mappingRulesObject);
    JsonObject mappingRules = mappingRulesNode == null
        ? JsonObject.of()
        : new JsonObject(objectMapper.writeValueAsString(mappingRulesNode));

    MappingParameters mappingParameters = MappingParametersUtility.getMappingParamaters(restTemplate);
    Instance instance = marcToInstanceMapper.mapRecord(parsedRecord, mappingParameters, mappingRules);

    return objectMapper.writeValueAsString(instance);
  }

  /**
   * Retrieve the instantiated FolioTokenCache.
   *
   * @return The instantiated FolioTokenCache.
   */
  private static FolioTokenCache getFolioTokenCache() {

    return MappingUtilityContext.getBean(FolioTokenCache.class);
  }

}
