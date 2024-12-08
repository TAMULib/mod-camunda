package org.folio.rest.camunda.utility;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;

/**
 * Represents different types of mapping parameters used in the FOLIO library
 * management system.
 *
 * This enum defines various configuration and metadata types that can be used
 * during record mapping and transformation processes. Each enum constant is
 * associated with:
 * <ul>
 *   <li>A specific parameter type class</li>
 *   <li>A corresponding collection type class</li>
 *   <li>A RESTful API endpoint path</li>
 * </ul>
 *
 * The enum provides methods to retrieve the parameter type, collection type,
 * and API path for each mapping parameter type.
 */
public enum MappingParametersType {

  /** Mapping type for alternative title types. */
  ALTERNATIVE_TITLE_TYPES(AlternativeTitleType.class, Alternativetitletypes.class, "/alternative-title-types"),

  /** Mapping type for call number types. */
  CALL_NUMBER_TYPES(CallNumberType.class, Callnumbertypes.class, "/call-number-types"),

  /** Mapping type for classification types. */
  CLASSIFICATION_TYPES(ClassificationType.class, Classificationtypes.class, "/classification-types"),

  /** Mapping type for contributor name types. */
  CONTRIBUTOR_NAME_TYPES(ContributorNameType.class, Contributornametypes.class, "/contributor-name-types"),

  /** Mapping type for contributor types. */
  CONTRIBUTOR_TYPES(ContributorType.class, Contributortypes.class, "/contributor-types"),

  /** Mapping type for electronic access relationships. */
  ELECTRONIC_ACCESS_RELATIONSHIPS(ElectronicAccessRelationship.class, Electronicaccessrelationships.class, "/electronic-access-relationships"),

  /** Mapping type for holdings note types. */
  HOLDINGS_NOTE_TYPES(HoldingsNoteType.class, Holdingsnotetypes.class, "/holdings-note-types"),

  /** Mapping type for holdings types. */
  HOLDINGS_TYPES(HoldingsType.class, Holdingstypes.class, "/holdings-types"),

  /** Mapping type for identifier types. */
  IDENTIFIER_TYPES(IdentifierType.class, Identifiertypes.class, "/identifier-types"),

  /** Mapping type for Interlibrary Loan (ILL) policies. */
  ILL_POLICIES(IllPolicy.class, Illpolicies.class, "/ill-policies"),

  /** Mapping type for instance formats. */
  INSTANCE_FORMATS(InstanceFormat.class, Instanceformats.class, "/instance-formats"),

  /** Mapping type for instance note types. */
  INSTANCE_NOTE_TYPES(InstanceNoteType.class, Instancenotetypes.class, "/instance-note-types"),

  /** Mapping type for instance relationship types. */
  INSTANCE_RELATIONSHIP_TYPES(InstanceRelationshipType.class, Instancerelationshiptypes.class, "/instance-relationship-types"),

  /** Mapping type for instance statuses. */
  INSTANCE_STATUSES(InstanceStatus.class, Instancestatuses.class, "/instance-statuses"),

  /** Mapping type for instance types. */
  INSTANCE_TYPES(InstanceType.class, Instancetypes.class, "/instance-types"),

  /** Mapping type for issuance modes. */
  ISSUANCE_MODES(IssuanceMode.class, Issuancemodes.class, "/modes-of-issuance"),

  /** Mapping type for item damaged statuses. */
  ITEM_DAMAGED_STATUSES(ItemDamageStatus.class, Itemdamagedstatuses.class, "/item-damaged-statuses"),

  /** Mapping type for item note types. */
  ITEM_NOTE_TYPES(ItemNoteType.class, Itemnotetypes.class, "/item-note-types"),

  /** Mapping type for loan types. */
  LOAN_TYPES(Loantype.class, Loantypes.class, "/loan-types"),

  /** Mapping type for locations. */
  LOCATIONS(Location.class, Locations.class, "/locations"),

  /** Mapping type for MARC field protection settings. */
  MARC_FIELD_PROTECTION_SETTINGS(MarcFieldProtectionSetting.class, MarcFieldProtectionSettingsCollection.class, "/field-protection-settings/marc"),

  /** Mapping type for material types. */
  MATERIAL_TYPES(Mtype.class, Materialtypes.class, "/material-types"),

  /** Mapping type for nature of content terms. */
  NATURE_OF_CONTENT_TERMS(NatureOfContentTerm.class, Natureofcontentterms.class, "/nature-of-content-terms"),

  /** Mapping type for statistical code types. */
  STATISTICAL_CODE_TYPES(StatisticalCodeType.class, Statisticalcodetypes.class, "/statistical-code-types"),

  /** Mapping type for statistical codes. */
  STATISTICAL_CODES(StatisticalCode.class, Statisticalcodes.class, "/statistical-codes"),

  /** Mapping type for subject sources. */
  SUBJECT_SOURCES(SubjectSource.class, SubjectSources.class, "/subject-sources"),

  /** Mapping type for subject types. */
  SUBJECT_TYPES(SubjectType.class, SubjectTypes.class, "/subject-types");

  /** The specific parameter type class for this mapping type. */
  private final Class<?> parametersType;

  /** The collection type class corresponding to this mapping type. */
  private final Class<?> collectionType;

  /** The API endpoint path for retrieving this mapping type's data. */
  private final String path;

  private static final Map<Class<?>, MappingParametersType> BY_PARAMETERS_TYPE = Arrays.stream(values())
      .collect(Collectors.toMap(MappingParametersType::getParametersType, Function.identity()));

  /**
   * Constructs a MappingParametersType with the specified parameter type,
   * collection type, and API path.
   *
   * @param parametersType The class representing the specific parameter type
   * @param collectionType The class representing the collection of this parameter
   *                       type
   * @param path           The API endpoint path for retrieving this mapping
   *                       type's data
   */
  MappingParametersType(Class<?> parametersType, Class<?> collectionType, String path) {
    this.parametersType = parametersType;
    this.collectionType = collectionType;
    this.path = path;
  }

  /**
   * Retrieves the parameter type class for this mapping type.
   *
   * @return The class representing the specific parameter type
   */
  public Class<?> getParametersType() {
    return parametersType;
  }

  /**
   * Retrieves the collection type class for this mapping type.
   *
   * @return The class representing the collection of this parameter type
   */
  @SuppressWarnings("unchecked")
  public <C> Class<C> getCollectionType() {
    return (Class<C>) collectionType;
  }

  /**
   * Retrieves the base API endpoint path for this mapping type.
   *
   * @return The API endpoint path
   */
  public String getPath() {
    return path;
  }

  /**
   * Retrieves the API endpoint path with a specified limit parameter.
   *
   * @param limit The maximum number of records to retrieve
   * @return The API endpoint path with limit query parameter
   */
  public String getPath(int limit) {
    return path + "?limit=" + limit;
  }

  /**
   * Retrieves the MappingParametersType associated with the given parameters type
   * class.
   *
   * @param parametersType The class representing the parameters type to look up
   * @return The corresponding MappingParametersType for the given class
   * @throws ParametersTypeLookupException if no MappingParametersType is found
   *                                       for the provided class
   */
  public static MappingParametersType fromParametersType(Class<?> parametersType) {
    if (!BY_PARAMETERS_TYPE.containsKey(parametersType)) {
      throw new ParametersTypeLookupException(parametersType);
    }

    return BY_PARAMETERS_TYPE.get(parametersType);
  }

  /**
   * Custom exception indicating a failure in looking up parameters type.
   *
   * @see RuntimeException
   */
  private static class ParametersTypeLookupException extends RuntimeException {

    /**
     * Template for generating detailed error messages about parameters type lookup
     * failures.
     * Includes placeholders for the parameter type and the endpoint path.
     */
    private static final String MESSAGE_TEMPLATE = "Invalid parameters type:  %s";

    /**
     * Constructs a new {@code ParametersTypeLookupException} with a detailed error
     * message based on the specific mapping parameters type that failed to
     * retrieve.
     *
     * @param parametersType The {@link Class} that failed to retrieve parameters
     */
    public ParametersTypeLookupException(Class<?> parametersType) {
      super(format(MESSAGE_TEMPLATE, parametersType));
    }

  }

}
