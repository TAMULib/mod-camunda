package org.folio.rest.camunda.utility;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.lang.NonNull;
import io.vertx.core.json.JsonObject;
import org.folio.AlternativeTitleType;
import org.folio.Alternativetitletypes;
import org.folio.CallNumberType;
import org.folio.Callnumbertypes;
import org.folio.ClassificationType;
import org.folio.Classificationtypes;
import org.folio.ContributorNameType;
import org.folio.ContributorType;
import org.folio.Contributornametypes;
import org.folio.Contributortypes;
import org.folio.ElectronicAccessRelationship;
import org.folio.Electronicaccessrelationships;
import org.folio.HoldingsNoteType;
import org.folio.HoldingsType;
import org.folio.Holdingsnotetypes;
import org.folio.Holdingstypes;
import org.folio.IdentifierType;
import org.folio.Identifiertypes;
import org.folio.IllPolicy;
import org.folio.Illpolicies;
import org.folio.InstanceFormat;
import org.folio.InstanceNoteType;
import org.folio.InstanceRelationshipType;
import org.folio.InstanceStatus;
import org.folio.InstanceType;
import org.folio.Instanceformats;
import org.folio.Instancenotetypes;
import org.folio.Instancerelationshiptypes;
import org.folio.Instancestatuses;
import org.folio.Instancetypes;
import org.folio.IssuanceMode;
import org.folio.Issuancemodes;
import org.folio.ItemDamageStatus;
import org.folio.ItemNoteType;
import org.folio.Itemdamagedstatuses;
import org.folio.Itemnotetypes;
import org.folio.Loantype;
import org.folio.Loantypes;
import org.folio.Location;
import org.folio.Locations;
import org.folio.MarcFieldProtectionSettingsCollection;
import org.folio.Materialtypes;
import org.folio.Mtype;
import org.folio.NatureOfContentTerm;
import org.folio.Natureofcontentterms;
import org.folio.StatisticalCode;
import org.folio.StatisticalCodeType;
import org.folio.Statisticalcodes;
import org.folio.Statisticalcodetypes;
import org.folio.SubjectSource;
import org.folio.SubjectSources;
import org.folio.SubjectType;
import org.folio.SubjectTypes;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/**
 * Utility class for managing and retrieving mapping parameters in a library
 * management system.
 *
 * This utility provides centralized methods for fetching configuration data and
 * mapping rules for various library-specific entities. It supports retrieving
 * comprehensive parameters for:
 * <ul>
 *   <li>Alternative title types</li>
 *   <li>Contributor types</li>
 *   <li>Instance formats</li>
 *   <li>And many other library-related type configurations</li>
 * </ul>
 *
 * The class utilizes an {@link OkapiRestTemplate} to dynamically fetch
 * parameters from predefined REST endpoints, supporting a flexible and
 * extensible approach to parameter management.
 *
 * Key features:
 * <ul>
 *   <li>Generic parameter retrieval for different entity types</li>
 *   <li>Centralized mapping rules fetching</li>
 *   <li>Comprehensive parameter collection initialization</li>
 * </ul>
 *
 * @see MappingParameters
 * @see MappingParametersType
 * @see OkapiRestTemplate
 */
public class MappingParametersUtility {

  /**
   * Maximum limit for fetching parameters from endpoints.
   * This constant ensures that parameter retrieval does not exceed 1000 items,
   * preventing excessive data transfer and potential performance issues.
   */
  public static final int LIMIT = 1000;

  /**
   * Base path for mapping rules related to MARC bibliographic records.
   * Used as a constant endpoint for retrieving mapping configuration specific to
   * MARC bibliographic records.
   */
  static final String MAPPING_RULES_PATH = "/mapping-rules/marc-bib";

  private MappingParametersUtility() {

  }

  /**
   * Fetches mapping rules from a predefined REST endpoint for MARC bibliographic
   * records.
   *
   * This method retrieves the mapping rules using the provided REST template,
   * handling potential empty responses gracefully.
   *
   * @param restTemplate The REST template used to make the HTTP request
   *                     (must not be null)
   * @return A {@link JsonObject} containing the mapping rules, or an empty
   *         JsonObject if no body is present
   * @throws RestClientException if there's an error communicating
   */
  public static JsonObject fetchRules(@NonNull OkapiRestTemplate restTemplate) {
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(MAPPING_RULES_PATH, JsonNode.class);

    return response.hasBody()
        ? JsonObject.mapFrom(response.getBody())
        : JsonObject.of();
  }

  /**
   * Retrieves comprehensive mapping parameters for various library entity types.
   *
   * This method performs a bulk retrieval of parameters for multiple
   * library-specific entity types, including:
   * <ul>
   *   <li>Alternative title types</li>
   *   <li>Contributor types</li>
   *   <li>Instance formats</li>
   *   <li>Holdings types</li>
   *   <li>And many more library configuration parameters</li>
   * </ul>
   *
   * The method uses the provided REST template to fetch parameters from different
   * endpoints and constructs a fully initialized {@link MappingParameters}
   * object.
   *
   * @param restTemplate The REST template used to fetch parameters from different
   *                     endpoints
   * @return A fully initialized {@link MappingParameters} object containing all
   *         retrieved parameters
   * @throws ParametersTypeLookupException if no descriptor is found for the given
   *                                       parameters type
   * @throws ParametersRetrievalException  if parameters cannot be retrieved
   * @throws RestClientException if there's an error communicating
   */
  public static MappingParameters getMappingParamaters(OkapiRestTemplate restTemplate) {
    return new MappingParameters()
        .withInitializedState(true)
        .withAlternativeTitleTypes(((Alternativetitletypes) getParameters(restTemplate, AlternativeTitleType.class)).getAlternativeTitleTypes())
        .withCallNumberTypes(((Callnumbertypes) getParameters(restTemplate, CallNumberType.class)).getCallNumberTypes())
        .withClassificationTypes(((Classificationtypes) getParameters(restTemplate, ClassificationType.class)).getClassificationTypes())
        .withContributorNameTypes(((Contributornametypes) getParameters(restTemplate, ContributorNameType.class)).getContributorNameTypes())
        .withContributorTypes(((Contributortypes) getParameters(restTemplate, ContributorType.class)).getContributorTypes())
        .withElectronicAccessRelationships(((Electronicaccessrelationships) getParameters(restTemplate, ElectronicAccessRelationship.class)).getElectronicAccessRelationships())
        .withHoldingsNoteTypes(((Holdingsnotetypes) getParameters(restTemplate, HoldingsNoteType.class)).getHoldingsNoteTypes())
        .withHoldingsTypes(((Holdingstypes) getParameters(restTemplate, HoldingsType.class)).getHoldingsTypes())
        .withIdentifierTypes(((Identifiertypes) getParameters(restTemplate, IdentifierType.class)).getIdentifierTypes())
        .withIllPolicies(((Illpolicies) getParameters(restTemplate, IllPolicy.class)).getIllPolicies())
        .withInstanceFormats(((Instanceformats) getParameters(restTemplate, InstanceFormat.class)).getInstanceFormats())
        .withInstanceNoteTypes(((Instancenotetypes) getParameters(restTemplate, InstanceNoteType.class)).getInstanceNoteTypes())
        .withInstanceRelationshipTypes(((Instancerelationshiptypes) getParameters(restTemplate, InstanceRelationshipType.class)).getInstanceRelationshipTypes())
        .withInstanceStatuses(((Instancestatuses) getParameters(restTemplate, InstanceStatus.class)).getInstanceStatuses())
        .withInstanceTypes(((Instancetypes) getParameters(restTemplate, InstanceType.class)).getInstanceTypes())
        .withIssuanceModes(((Issuancemodes) getParameters(restTemplate, IssuanceMode.class)).getIssuanceModes())
        .withItemDamagedStatuses(((Itemdamagedstatuses) getParameters(restTemplate, ItemDamageStatus.class)).getItemDamageStatuses())
        .withItemNoteTypes(((Itemnotetypes) getParameters(restTemplate, ItemNoteType.class)).getItemNoteTypes())
        .withLoanTypes(((Loantypes) getParameters(restTemplate, Loantype.class)).getLoantypes())
        .withLocations(((Locations) getParameters(restTemplate, Location.class)).getLocations())
        .withMarcFieldProtectionSettings(((MarcFieldProtectionSettingsCollection) getParameters(restTemplate, MarcFieldProtectionSetting.class)).getMarcFieldProtectionSettings())
        .withMaterialTypes(((Materialtypes) getParameters(restTemplate, Mtype.class)).getMtypes())
        .withNatureOfContentTerms(((Natureofcontentterms) getParameters(restTemplate, NatureOfContentTerm.class)).getNatureOfContentTerms())
        .withStatisticalCodes(((Statisticalcodes) getParameters(restTemplate, StatisticalCode.class)).getStatisticalCodes())
        .withStatisticalCodeTypes(((Statisticalcodetypes) getParameters(restTemplate, StatisticalCodeType.class)).getStatisticalCodeTypes())
        .withSubjectSources(((SubjectSources) getParameters(restTemplate, SubjectSource.class)).getSubjectSources())
        .withSubjectTypes(((SubjectTypes) getParameters(restTemplate, SubjectType.class)).getSubjectTypes());
  }

  /**
   * Generic method to retrieve parameters for a specific library entity type.
   *
   * This method dynamically fetches parameters based on the provided parameter
   * type, using a pre-configured mapping of parameter types to their respective
   * endpoints and collection types.
   *
   * Key features:
   * <ul>
   * <li>Supports retrieval of various library entity type parameters</li>
   * <li>Dynamically determines the appropriate endpoint and collection type</li>
   * <li>Handles potential retrieval failures</li>
   * </ul>
   *
   * @param <C>            The type of the collection or parameters to be returned
   * @param <P>            The type of the parameter class
   * @param restTemplate   The REST template used to fetch parameters
   * @param parametersType The class of the parameters to retrieve
   * @return Parameters of the specified type
   * @throws ParametersTypeLookupException if no descriptor is found for the given
   *                                       parameters type
   * @throws ParametersRetrievalException  if parameters cannot be retrieved
   * @throws RestClientException if there's an error communicating
   */
  public static <C, P> C getParameters(OkapiRestTemplate restTemplate, Class<P> parametersType) {
    MappingParametersType type = MappingParametersType.fromParametersType(parametersType);
    String path = type.getPath(LIMIT);

    Class<C> collectionType = type.getCollectionType();
    ResponseEntity<C> response = restTemplate.getForEntity(path, collectionType);

    if (!response.hasBody()) {
      throw new ParametersRetrievalException(type);
    }

    return response.getBody();
  }

  /**
   * Custom exception indicating a failure in retrieving library configuration
   * parameters.
   *
   * @see RuntimeException
   * @see MappingParametersType
   */
  private static class ParametersRetrievalException extends RuntimeException {

    /**
     * Template for generating detailed error messages about parameter retrieval
     * failures.
     * Includes placeholders for the parameter type and the endpoint path.
     */
    private static final String MESSAGE_TEMPLATE = "Failed to retrieve %s parameters from path %s";

    /**
     * Constructs a new {@code ParametersRetrievalException} with a detailed error
     * message based on the specific mapping parameters type that failed to
     * retrieve.
     *
     * @param type The {@link MappingParametersType} that failed to retrieve
     *             parameters
     */
    public ParametersRetrievalException(MappingParametersType type) {
      super(format(MESSAGE_TEMPLATE, type.getCollectionType().getSimpleName(), type.getPath(LIMIT)));
    }

  }

}
