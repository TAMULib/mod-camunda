package org.folio.rest.camunda.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;

/**
 * Test utility for various input methods for parameterized testing of
 * utilities.
 */
public class TestUtility {

  private static final ObjectMapper om = new ObjectMapper();

  private TestUtility() {

  }

  /**
   * Build input from file as class type wrapped by ResponseEntity.
   *
   * @param <T> generic input type
   * @param path path to mock resource JSON
   * @param valueType type of generic object to map to
   * @return ResponseEntity for a expected type to test with
   * @throws IOException when reading file or object mapping fails
   */
  public static <T> ResponseEntity<T> i(String path, Class<T> valueType) throws IOException {
    return ResponseEntity.ofNullable(om.readValue(new File("src/test/resources/" + path), valueType));
  }

  /**
   * Build input from file as a String
   *
   * @param path path to mock resource
   * @return String of the input file as UTF-8
   * @throws IOException when reading file
   */
  public static String i(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  /**
   * Build input from two files as an array of String.
   *
   * @param path input file
   * @param additionalPath path to additional file
   * @return array of strings from the file input
   * @throws IOException
   */
  public static String[] i(String path, String additionalPath) throws IOException {
    return new String[] {
      IOUtils.resourceToString(path, StandardCharsets.UTF_8),
      IOUtils.resourceToString(additionalPath, StandardCharsets.UTF_8)
    };
  }

  /**
   * Build input from a JSON array file as a List<String>.
   *
   * @param path path to input file that is a JSON array
   * @return list of JSON array entries as String
   * @throws IOException when reading file or object mapping fails
   */
  public static List<String> il(String path) throws IOException {
    String json = i(path);
    List<String> marcjson = new ArrayList<>();

    for (JsonNode n : om.readTree(json)) {
      marcjson.add(n.toString());
    }

    return marcjson;
  }

  /**
   * Object map a JSON String to JsonNode catching any exceptions.
   *
   * @param json JSON String
   * @return JsonNode
   */
  public static JsonNode om(String json) {
    try {
      return om.readTree(json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  /**
   * Object map list of Srring to list orf JsonNode.
   *
   * @param json list of JSON strings
   * @return list of JsonNode
   */
  public static List<JsonNode> oml(List<String> json) {
    return json.stream().map(n -> om(n)).toList();
  }

}
