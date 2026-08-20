package org.folio.rest.camunda.utility;

import static org.folio.rest.camunda.utility.TestUtility.i;
import static org.folio.rest.camunda.utility.TestUtility.il;
import static org.folio.rest.camunda.utility.TestUtility.oml;
import static org.folio.rest.camunda.utility.TestUtility.treeNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.MarcException;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarcUtilityTest {

  @Test
  void testDeserializingSubfield() throws JsonProcessingException {
    MarcFactory factory = MarcFactory.newInstance();
    Subfield subfield = factory.newSubfield();
    subfield.setCode("245".charAt(0));
    subfield.setData("data");
    String serialized = MarcUtility.mapper.writeValueAsString(subfield);
    Subfield deserializedSubfield = MarcUtility.mapper.readValue(serialized, Subfield.class);
    assertEquals(subfield.getId(), deserializedSubfield.getId());
    assertEquals(subfield.getCode(), deserializedSubfield.getCode());
    assertEquals(subfield.getData(), deserializedSubfield.getData());
  }

  @ParameterizedTest
  @MethodSource("testSplitRawMarcToMarcJsonRecordsStream")
  void testSplitRawMarcToMarcJsonRecords(Parameters<String, List<String>> data) throws MarcException, IOException {
    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.splitRawMarcToMarcJsonRecords(data.input));
    } else {
      @SuppressWarnings("unchecked")
      List<JsonNode>[] ea = (List<JsonNode>[]) new List[2];

      ea[0] = new ArrayList<JsonNode>();
      ea[1] = new ArrayList<JsonNode>();

      for (JsonNode n : oml(data.expected)) {
        ea[0].add(n);
      }

      for (JsonNode n : oml(MarcUtility.splitRawMarcToMarcJsonRecords(data.input))) {
        ea[1].add(n);
      }

      for (int i = 0; i < ea[0].size(); i++) {
        assertEquals(ea[0].get(i), ea[1].get(i));
      }
    }
  }

  @ParameterizedTest
  @MethodSource("testAddFieldToMarcJsonStream")
  void testAddFieldToMarcJson(Parameters<Object, String> data) throws MarcException, IOException {
    String marcJson = data.input != null ? ((String[]) data.input)[0] : null;
    String fieldJson = data.input != null ? ((String[]) data.input)[1] : null;

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.addFieldToMarcJson(marcJson, fieldJson));
    } else {
      assertEquals(treeNode(data.expected), treeNode(MarcUtility.addFieldToMarcJson(marcJson, fieldJson)));
    }
  }

  @ParameterizedTest
  @MethodSource("testUpdateControlNumberFieldStream")
  void testUpdateControlNumberField(Parameters<String, String> data) throws MarcException, IOException {
    String marcJson = data.input;
    String controlNumber = "001";

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.updateControlNumberField(marcJson, controlNumber));
    } else {
      assertEquals(treeNode(data.expected), treeNode(MarcUtility.updateControlNumberField(marcJson, controlNumber)));
    }
  }

  @ParameterizedTest
  @MethodSource("testMarcJsonToRawMarcStream")
  void testMarcJsonToRawMarc(Parameters<String, String> data) throws MarcException, IOException {
    String marcJson = data.input;

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.marcJsonToRawMarc(marcJson));
    } else {
      assertEquals(data.expected.trim(), MarcUtility.marcJsonToRawMarc(marcJson).trim());
    }
  }

  @ParameterizedTest
  @MethodSource("testRawMarcToMarcJsonStream")
  void testRawMarcToMarcJson(Parameters<String, String> data) throws MarcException, IOException {
    String rawMarc = data.input;

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.rawMarcToMarcJson(rawMarc));
    } else {
      assertEquals(treeNode(data.expected), treeNode(MarcUtility.rawMarcToMarcJson(rawMarc)));
    }
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromRawMarcStream")
  void testGetFieldsFromRawMarc(Parameters<Object, String> data) throws MarcException, IOException {
    String rawMarc;
    String tagsJson;

    String[] tags;

    if (data.input == null) {
      rawMarc = null;
      tags = new String[0];
    } else {
      rawMarc = ((String[]) data.input)[0];
      tagsJson = ((String[]) data.input)[1];

      List<String> list = new ArrayList<>();
      for (JsonNode n : MarcUtility.mapper.readTree(tagsJson)) {
        list.add(n.toString());
      }

      tags = MarcUtility.mapper.readValue(tagsJson, String[].class);
    }

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.getFieldsFromRawMarc(rawMarc, tags));
    } else {
      assertEquals(treeNode(data.expected), treeNode(MarcUtility.getFieldsFromRawMarc(rawMarc, tags).trim()));
    }
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromMarcJsonStream")
  void testGetFieldsFromMarcJson(Parameters<Object, String> data) throws MarcException, IOException {
    String marcJson;
    String tagsJson;

    String[] tags;

    if (data.input == null) {
      marcJson = null;
      tags = new String[0];
    } else {
      marcJson = ((String[]) data.input)[0];
      tagsJson = ((String[]) data.input)[1];

      List<String> list = new ArrayList<>();
      for (JsonNode n : MarcUtility.mapper.readTree(tagsJson)) {
        list.add(n.toString());
      }

      tags = MarcUtility.mapper.readValue(tagsJson, String[].class);
    }

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.getFieldsFromMarcJson(marcJson, tags));
    } else {
      assertEquals(treeNode(data.expected), treeNode(MarcUtility.getFieldsFromMarcJson(marcJson, tags).trim()));
    }
  }

  /**
   * Stream parameters for testing addFieldToMarcJson.
   *
   * @return
   *   The test method parameters:
   *     - input of type String[2] as Object [MARC JSON, JSON of field to add]
   *     - expected output of type String (MARC JSON with field added).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<Object, String>> testAddFieldToMarcJsonStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new IllegalArgumentException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/field/999.mrc.json"), i("/marc4j/withfields/54-56-008008027+999.mrc.json"))
      );
  }

  /**
   * Stream parameters for testing updateControlNumberField.
   *
   * @return
   *   The test method parameters:
   *     - input of type String (MARC JSON)
   *     - expected output of type String (MARC JSON with control number set).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<String, String>> testUpdateControlNumberFieldStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json")),
        Parameters.of(i("/marc4j/54-56-008008027-0-001.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json"))
      );
  }

  /**
   * Stream parameters for testing marcJsonToRawMarc.
   *
   * @return
   *   The test method parameters:
   *     - input of type String (MARC JSON)
   *     - expected output of type String (RAW MARC).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<String, String>> testMarcJsonToRawMarcStream() throws IOException {
    return Stream.of(
      Parameters.of(null, null, new NullPointerException()),
      Parameters.of("", null, new MarcException()),
      Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/54-56-008008027-0.mrc"))
    );
  }

  /**
   * Stream parameters for testing rawMarcToMarcJson.
   *
   * @return
   *   The test method parameters:
   *     - input of type String (RAW MARC)
   *     - expected output of type String (MARC JSON).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<String, String>> testRawMarcToMarcJsonStream() throws IOException {
    return Stream.of(
      Parameters.of(null, null, new NullPointerException()),
      Parameters.of("", null, new MarcException()),
      Parameters.of(i("/marc4j/54-56-008008027-0.mrc"), i("/marc4j/54-56-008008027-0.mrc.json"))
    );
  }

  /**
   * Stream parameters for testing splitRawMarcToMarcJsonRecords.
   *
   * @return
   *   The test method parameters:
   *     - input of type String (RAW MARC)
   *     - expected output of type List<String> (list of MARC JSON).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<String, List<String>>> testSplitRawMarcToMarcJsonRecordsStream() throws IOException {
    return Stream.of(
      Parameters.of(null, null, new NullPointerException()),
      Parameters.of("", List.of()),
      Parameters.of(i("/marc4j/54-56-008008027.mrc"), il("/marc4j/54-56-008008027.mrc.json"))
    );
  }

  /**
   * Stream parameters for testing getFieldsFromRawMarc.
   *
   * @return
   *   The test method parameters:
   *     - input of type String[2] as Object [RAW MARC, JSON array of tags]
   *     - expected output of type String (JSON of fields found matching tags).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<Object, String>> testGetFieldsFromRawMarcStream() throws IOException {
    return Stream.of(
      Parameters.of(null, null, new NullPointerException()),
      Parameters.of(new String[] { "", "[]" }, null, new MarcException()),
      Parameters.of(i("/marc4j/54-56-008008027-0.mrc", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 050090245947980.json"))
    );
  }

  /**
   * Stream parameters for testing getFieldsFromMarcJson.
   *
   * @return
   *   The test method parameters:
   *     - input of type String[2] as Object [MARC JSON, JSON array of tags]
   *     - expected output of type String (JSON of fields found matching tags).
   *     - exception expected to be thrown
   */
  static Stream<Parameters<Object, String>> testGetFieldsFromMarcJsonStream() throws IOException {
    return Stream.of(
      Parameters.of(null, null, new NullPointerException()),
      Parameters.of(new String[] { "", "[]" }, null, new MarcException()),
      Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 050090245947980.json"))
    );
  }

}
