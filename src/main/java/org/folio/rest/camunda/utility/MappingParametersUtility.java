package org.folio.rest.camunda.utility;

import java.util.HashMap;
import java.util.Map;

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
 * Utility class for managing mapping parameters and fetching configuration data
 * for various library-related types in a library management system.
 *
 * This class provides methods to retrieve mapping rules and parameters for different
 * library-specific entities such as alternative title types, contributor types,
 * instance formats, and more.
 *
 * The utility uses an {@link OkapiRestTemplate} to fetch data from predefined REST endpoints,
 * and maintains a static mapping of parameter types to their respective descriptors
 * and endpoint paths.
 *
 * @see MappingParametersDescriptor
 * @see MappingParameters
 */
public class MappingParametersUtility {

  /**
   * Maximum limit for fetching parameters from endpoints.
   * This constant ensures that parameter retrieval does not exceed 1000 items.
   */
  public static final int LIMIT = 1000;

  /**
   * Base path for mapping rules related to MARC bibliographic records.
   */
  static final String MAPPING_RULES_PATH = "/mapping-rules/marc-bib";

  /**
   * A static map that associates parameter classes with their corresponding
   * mapping parameter descriptors and endpoint paths.
   *
   * This map is used to dynamically retrieve parameters for various library
   * entity types during initialization.
   */
  private static final Map<Class<?>, MappingParametersDescriptor<?,?>> PARAMETER_DESCRIPTORS = new HashMap<>();

  static {
    for (MappingParametersType type : MappingParametersType.values()) {
      PARAMETER_DESCRIPTORS.put(type.getParametersType(), MappingParametersDescriptor.of(type.getCollectionType(), type.getPath(LIMIT)));
    }
  }

  private MappingParametersUtility() {

  }

  /**
   * Fetches the mapping rules from the predefined REST endpoint.
   *
   * @param restTemplate The REST template used to make the HTTP request
   * @return A {@link JsonObject} containing the mapping rules
   * @throws RestClientException if there's an error fetching the rules
   */
  public static JsonObject fetchRules(OkapiRestTemplate restTemplate) {
    ResponseEntity<String> response = restTemplate.getForEntity(MAPPING_RULES_PATH, String.class);

    return response.hasBody()
      ? new JsonObject(response.getBody())
      : new JsonObject();
  }

  /**
   * Retrieves comprehensive mapping parameters for various library entity types.
   *
   * This method fetches parameters for multiple types such as alternative title types,
   * contributor types, instance formats, and more using the provided REST template.
   *
   * @param restTemplate The REST template used to fetch parameters from different endpoints
   * @return A fully initialized {@link MappingParameters} object with all parameters
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
   * Generic method to retrieve parameters for a specific type using the
   * pre-configured parameter descriptor map.
   *
   * @param <C> The type of the collection or parameters to be returned
   * @param <P> The type of the parameter class
   * @param okapiRestRemplate The REST template used to fetch parameters
   * @param parametersType The class of the parameters to retrieve
   * @return Parameters of the specified type
   * @throws IllegalArgumentException if no descriptor is found for the given type
   */
  @SuppressWarnings("unchecked")
  public static <C, P> C getParameters(OkapiRestTemplate okapiRestRemplate, P parametersType) {
    MappingParametersDescriptor<C, P> descriptor = (MappingParametersDescriptor<C, P>) PARAMETER_DESCRIPTORS.get(parametersType);
    if (descriptor == null) {
        throw new IllegalArgumentException("No descriptor found for type: " + parametersType);
    }

    return descriptor.getParameters(okapiRestRemplate);
  }

}
