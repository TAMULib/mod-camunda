package org.folio.rest.camunda.utility;

import static org.folio.rest.camunda.utility.MappingParametersUtility.LIMIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;
import java.util.stream.Stream;

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
import org.folio.rest.camunda.utility.MappingParametersType.ParametersTypeLookupException;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MappingParametersTypeTest {

  @ParameterizedTest
  @MethodSource("testGetParametersTypeStream")
  void testGetParametersType(MappingParametersType type, Class<?> parametersType) {
    assertEquals(parametersType, type.getParametersType());
  }

  static Stream<Arguments> testGetParametersTypeStream() {
    return Stream.of(
        Arguments.of(MappingParametersType.ALTERNATIVE_TITLE_TYPES, AlternativeTitleType.class),
        Arguments.of(MappingParametersType.CALL_NUMBER_TYPES, CallNumberType.class),
        Arguments.of(MappingParametersType.CLASSIFICATION_TYPES, ClassificationType.class),
        Arguments.of(MappingParametersType.CONTRIBUTOR_NAME_TYPES, ContributorNameType.class),
        Arguments.of(MappingParametersType.CONTRIBUTOR_TYPES, ContributorType.class),
        Arguments.of(MappingParametersType.ELECTRONIC_ACCESS_RELATIONSHIPS, ElectronicAccessRelationship.class),
        Arguments.of(MappingParametersType.HOLDINGS_NOTE_TYPES, HoldingsNoteType.class),
        Arguments.of(MappingParametersType.HOLDINGS_TYPES, HoldingsType.class),
        Arguments.of(MappingParametersType.IDENTIFIER_TYPES, IdentifierType.class),
        Arguments.of(MappingParametersType.ILL_POLICIES, IllPolicy.class),
        Arguments.of(MappingParametersType.INSTANCE_FORMATS, InstanceFormat.class),
        Arguments.of(MappingParametersType.INSTANCE_NOTE_TYPES, InstanceNoteType.class),
        Arguments.of(MappingParametersType.INSTANCE_RELATIONSHIP_TYPES, InstanceRelationshipType.class),
        Arguments.of(MappingParametersType.INSTANCE_STATUSES, InstanceStatus.class),
        Arguments.of(MappingParametersType.INSTANCE_TYPES, InstanceType.class),
        Arguments.of(MappingParametersType.ISSUANCE_MODES, IssuanceMode.class),
        Arguments.of(MappingParametersType.ITEM_DAMAGED_STATUSES, ItemDamageStatus.class),
        Arguments.of(MappingParametersType.ITEM_NOTE_TYPES, ItemNoteType.class),
        Arguments.of(MappingParametersType.LOAN_TYPES, Loantype.class),
        Arguments.of(MappingParametersType.LOCATIONS, Location.class),
        Arguments.of(MappingParametersType.MARC_FIELD_PROTECTION_SETTINGS, MarcFieldProtectionSetting.class),
        Arguments.of(MappingParametersType.MATERIAL_TYPES, Mtype.class),
        Arguments.of(MappingParametersType.NATURE_OF_CONTENT_TERMS, NatureOfContentTerm.class),
        Arguments.of(MappingParametersType.STATISTICAL_CODE_TYPES, StatisticalCodeType.class),
        Arguments.of(MappingParametersType.STATISTICAL_CODES, StatisticalCode.class),
        Arguments.of(MappingParametersType.SUBJECT_SOURCES, SubjectSource.class),
        Arguments.of(MappingParametersType.SUBJECT_TYPES, SubjectType.class));
  }

  @ParameterizedTest
  @MethodSource("testGetCollectionTypeStream")
  void testGetCollectionType(MappingParametersType type, Class<?> collectionType) {
    assertEquals(collectionType, type.getCollectionType());
  }

  static Stream<Arguments> testGetCollectionTypeStream() {
    return Stream.of(
        Arguments.of(MappingParametersType.ALTERNATIVE_TITLE_TYPES, Alternativetitletypes.class),
        Arguments.of(MappingParametersType.CALL_NUMBER_TYPES, Callnumbertypes.class),
        Arguments.of(MappingParametersType.CLASSIFICATION_TYPES, Classificationtypes.class),
        Arguments.of(MappingParametersType.CONTRIBUTOR_NAME_TYPES, Contributornametypes.class),
        Arguments.of(MappingParametersType.CONTRIBUTOR_TYPES, Contributortypes.class),
        Arguments.of(MappingParametersType.ELECTRONIC_ACCESS_RELATIONSHIPS, Electronicaccessrelationships.class),
        Arguments.of(MappingParametersType.HOLDINGS_NOTE_TYPES, Holdingsnotetypes.class),
        Arguments.of(MappingParametersType.HOLDINGS_TYPES, Holdingstypes.class),
        Arguments.of(MappingParametersType.IDENTIFIER_TYPES, Identifiertypes.class),
        Arguments.of(MappingParametersType.ILL_POLICIES, Illpolicies.class),
        Arguments.of(MappingParametersType.INSTANCE_FORMATS, Instanceformats.class),
        Arguments.of(MappingParametersType.INSTANCE_NOTE_TYPES, Instancenotetypes.class),
        Arguments.of(MappingParametersType.INSTANCE_RELATIONSHIP_TYPES, Instancerelationshiptypes.class),
        Arguments.of(MappingParametersType.INSTANCE_STATUSES, Instancestatuses.class),
        Arguments.of(MappingParametersType.INSTANCE_TYPES, Instancetypes.class),
        Arguments.of(MappingParametersType.ISSUANCE_MODES, Issuancemodes.class),
        Arguments.of(MappingParametersType.ITEM_DAMAGED_STATUSES, Itemdamagedstatuses.class),
        Arguments.of(MappingParametersType.ITEM_NOTE_TYPES, Itemnotetypes.class),
        Arguments.of(MappingParametersType.LOAN_TYPES, Loantypes.class),
        Arguments.of(MappingParametersType.LOCATIONS, Locations.class),
        Arguments.of(MappingParametersType.MARC_FIELD_PROTECTION_SETTINGS, MarcFieldProtectionSettingsCollection.class),
        Arguments.of(MappingParametersType.MATERIAL_TYPES, Materialtypes.class),
        Arguments.of(MappingParametersType.NATURE_OF_CONTENT_TERMS, Natureofcontentterms.class),
        Arguments.of(MappingParametersType.STATISTICAL_CODE_TYPES, Statisticalcodetypes.class),
        Arguments.of(MappingParametersType.STATISTICAL_CODES, Statisticalcodes.class),
        Arguments.of(MappingParametersType.SUBJECT_SOURCES, SubjectSources.class),
        Arguments.of(MappingParametersType.SUBJECT_TYPES, SubjectTypes.class));
  }

  @ParameterizedTest
  @MethodSource("testGetPathStream")
  void testGetPath(MappingParametersType type, String path) {
    assertEquals(path, type.getPath());
  }

  static Stream<Arguments> testGetPathStream() {
    return Stream.of(
        Arguments.of(MappingParametersType.ALTERNATIVE_TITLE_TYPES, "/alternative-title-types"),
        Arguments.of(MappingParametersType.CALL_NUMBER_TYPES, "/call-number-types"),
        Arguments.of(MappingParametersType.CLASSIFICATION_TYPES, "/classification-types"),
        Arguments.of(MappingParametersType.CONTRIBUTOR_NAME_TYPES, "/contributor-name-types"),
        Arguments.of(MappingParametersType.CONTRIBUTOR_TYPES, "/contributor-types"),
        Arguments.of(MappingParametersType.ELECTRONIC_ACCESS_RELATIONSHIPS, "/electronic-access-relationships"),
        Arguments.of(MappingParametersType.HOLDINGS_NOTE_TYPES, "/holdings-note-types"),
        Arguments.of(MappingParametersType.HOLDINGS_TYPES, "/holdings-types"),
        Arguments.of(MappingParametersType.IDENTIFIER_TYPES, "/identifier-types"),
        Arguments.of(MappingParametersType.ILL_POLICIES, "/ill-policies"),
        Arguments.of(MappingParametersType.INSTANCE_FORMATS, "/instance-formats"),
        Arguments.of(MappingParametersType.INSTANCE_NOTE_TYPES, "/instance-note-types"),
        Arguments.of(MappingParametersType.INSTANCE_RELATIONSHIP_TYPES, "/instance-relationship-types"),
        Arguments.of(MappingParametersType.INSTANCE_STATUSES, "/instance-statuses"),
        Arguments.of(MappingParametersType.INSTANCE_TYPES, "/instance-types"),
        Arguments.of(MappingParametersType.ISSUANCE_MODES, "/modes-of-issuance"),
        Arguments.of(MappingParametersType.ITEM_DAMAGED_STATUSES, "/item-damaged-statuses"),
        Arguments.of(MappingParametersType.ITEM_NOTE_TYPES, "/item-note-types"),
        Arguments.of(MappingParametersType.LOAN_TYPES, "/loan-types"),
        Arguments.of(MappingParametersType.LOCATIONS, "/locations"),
        Arguments.of(MappingParametersType.MARC_FIELD_PROTECTION_SETTINGS, "/field-protection-settings/marc"),
        Arguments.of(MappingParametersType.MATERIAL_TYPES, "/material-types"),
        Arguments.of(MappingParametersType.NATURE_OF_CONTENT_TERMS, "/nature-of-content-terms"),
        Arguments.of(MappingParametersType.STATISTICAL_CODE_TYPES, "/statistical-code-types"),
        Arguments.of(MappingParametersType.STATISTICAL_CODES, "/statistical-codes"),
        Arguments.of(MappingParametersType.SUBJECT_SOURCES, "/subject-sources"),
        Arguments.of(MappingParametersType.SUBJECT_TYPES, "/subject-types"));
  }

  @ParameterizedTest
  @MethodSource("testGetPathWithLimitStream")
  void testGetPathWithLimit(MappingParametersType type, String path) {
    assertEquals(path, type.getPath(LIMIT));
  }

  static Stream<Arguments> testGetPathWithLimitStream() {
    return Stream.of(
        Arguments.of(MappingParametersType.ALTERNATIVE_TITLE_TYPES, "/alternative-title-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.CALL_NUMBER_TYPES, "/call-number-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.CLASSIFICATION_TYPES, "/classification-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.CONTRIBUTOR_NAME_TYPES, "/contributor-name-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.CONTRIBUTOR_TYPES, "/contributor-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.ELECTRONIC_ACCESS_RELATIONSHIPS,
            "/electronic-access-relationships?limit=" + LIMIT),
        Arguments.of(MappingParametersType.HOLDINGS_NOTE_TYPES, "/holdings-note-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.HOLDINGS_TYPES, "/holdings-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.IDENTIFIER_TYPES, "/identifier-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.ILL_POLICIES, "/ill-policies?limit=" + LIMIT),
        Arguments.of(MappingParametersType.INSTANCE_FORMATS, "/instance-formats?limit=" + LIMIT),
        Arguments.of(MappingParametersType.INSTANCE_NOTE_TYPES, "/instance-note-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.INSTANCE_RELATIONSHIP_TYPES, "/instance-relationship-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.INSTANCE_STATUSES, "/instance-statuses?limit=" + LIMIT),
        Arguments.of(MappingParametersType.INSTANCE_TYPES, "/instance-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.ISSUANCE_MODES, "/modes-of-issuance?limit=" + LIMIT),
        Arguments.of(MappingParametersType.ITEM_DAMAGED_STATUSES, "/item-damaged-statuses?limit=" + LIMIT),
        Arguments.of(MappingParametersType.ITEM_NOTE_TYPES, "/item-note-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.LOAN_TYPES, "/loan-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.LOCATIONS, "/locations?limit=" + LIMIT),
        Arguments.of(MappingParametersType.MARC_FIELD_PROTECTION_SETTINGS,
            "/field-protection-settings/marc?limit=" + LIMIT),
        Arguments.of(MappingParametersType.MATERIAL_TYPES, "/material-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.NATURE_OF_CONTENT_TERMS, "/nature-of-content-terms?limit=" + LIMIT),
        Arguments.of(MappingParametersType.STATISTICAL_CODE_TYPES, "/statistical-code-types?limit=" + LIMIT),
        Arguments.of(MappingParametersType.STATISTICAL_CODES, "/statistical-codes?limit=" + LIMIT),
        Arguments.of(MappingParametersType.SUBJECT_SOURCES, "/subject-sources?limit=" + LIMIT),
        Arguments.of(MappingParametersType.SUBJECT_TYPES, "/subject-types?limit=" + LIMIT));
  }

  @ParameterizedTest
  @MethodSource("testFromParametersTypeStream")
  void testFromParametersType(MappingParametersType type, Class<?> parametersType, Class<Exception> exception) {
    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> {
        MappingParametersType.fromParametersType(parametersType);
      });
    } else {
      assertEquals(type, MappingParametersType.fromParametersType(parametersType));
    }
  }

  static Stream<Arguments> testFromParametersTypeStream() {
    return Stream.of(
        Arguments.of(null, Object.class, ParametersTypeLookupException.class),
        Arguments.of(MappingParametersType.ALTERNATIVE_TITLE_TYPES, AlternativeTitleType.class, null),
        Arguments.of(MappingParametersType.CALL_NUMBER_TYPES, CallNumberType.class, null),
        Arguments.of(MappingParametersType.CLASSIFICATION_TYPES, ClassificationType.class, null),
        Arguments.of(MappingParametersType.CONTRIBUTOR_NAME_TYPES, ContributorNameType.class, null),
        Arguments.of(MappingParametersType.CONTRIBUTOR_TYPES, ContributorType.class, null),
        Arguments.of(MappingParametersType.ELECTRONIC_ACCESS_RELATIONSHIPS, ElectronicAccessRelationship.class, null),
        Arguments.of(MappingParametersType.HOLDINGS_NOTE_TYPES, HoldingsNoteType.class, null),
        Arguments.of(MappingParametersType.HOLDINGS_TYPES, HoldingsType.class, null),
        Arguments.of(MappingParametersType.IDENTIFIER_TYPES, IdentifierType.class, null),
        Arguments.of(MappingParametersType.ILL_POLICIES, IllPolicy.class, null),
        Arguments.of(MappingParametersType.INSTANCE_FORMATS, InstanceFormat.class, null),
        Arguments.of(MappingParametersType.INSTANCE_NOTE_TYPES, InstanceNoteType.class, null),
        Arguments.of(MappingParametersType.INSTANCE_RELATIONSHIP_TYPES, InstanceRelationshipType.class, null),
        Arguments.of(MappingParametersType.INSTANCE_STATUSES, InstanceStatus.class, null),
        Arguments.of(MappingParametersType.INSTANCE_TYPES, InstanceType.class, null),
        Arguments.of(MappingParametersType.ISSUANCE_MODES, IssuanceMode.class, null),
        Arguments.of(MappingParametersType.ITEM_DAMAGED_STATUSES, ItemDamageStatus.class, null),
        Arguments.of(MappingParametersType.ITEM_NOTE_TYPES, ItemNoteType.class, null),
        Arguments.of(MappingParametersType.LOAN_TYPES, Loantype.class, null),
        Arguments.of(MappingParametersType.LOCATIONS, Location.class, null),
        Arguments.of(MappingParametersType.MARC_FIELD_PROTECTION_SETTINGS, MarcFieldProtectionSetting.class, null),
        Arguments.of(MappingParametersType.MATERIAL_TYPES, Mtype.class, null),
        Arguments.of(MappingParametersType.NATURE_OF_CONTENT_TERMS, NatureOfContentTerm.class, null),
        Arguments.of(MappingParametersType.STATISTICAL_CODE_TYPES, StatisticalCodeType.class, null),
        Arguments.of(MappingParametersType.STATISTICAL_CODES, StatisticalCode.class, null),
        Arguments.of(MappingParametersType.SUBJECT_SOURCES, SubjectSource.class, null),
        Arguments.of(MappingParametersType.SUBJECT_TYPES, SubjectType.class, null));
  }

}
