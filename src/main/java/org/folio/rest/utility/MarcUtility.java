package org.folio.rest.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.marc4j.MarcException;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.SubfieldImpl;

public class MarcUtility {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final ObjectMapper mapper = new ObjectMapper();

  public MarcUtility() {
    mapper.setSerializationInclusion(Include.NON_NULL);
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Subfield.class, new JsonDeserializer<Subfield>() {
      @Override
      public Subfield deserialize(JsonParser jp, DeserializationContext ctxt)
          throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        return mapper.treeToValue(node, SubfieldImpl.class);
      }
    });
    mapper.registerModule(module);
  }

  public List<String> splitRawMarcToMarcJsonRecords(String rawMarc) {
    List<Record> records = new ArrayList<>();
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes(DEFAULT_CHARSET))) {
      final MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      while (reader.hasNext()) {
        records.add(reader.next());
      }
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    } catch (final MarcException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }

    return records.stream()
      .map(this::recordToJson)
      .collect(Collectors.toList());
  }

  /**
   * Convert a String representing marc binary into a JSON string.
   *
   * @param rawMarc The marcBinary String to convert into json.
   *
   * @return A String containing the encoded JSON marc data.
   * @throws IOException
   */
  public String rawMarcToJson(String rawMarc) {
    Optional<Record> record = rawMarcToRecord(rawMarc);
    if (record.isPresent()) {
      return recordToJson(record.get());
    }
    return "{}";
  }

  public String getFieldsFromRawMarc(String rawMarc, String[] tags) {
    Optional<Record> record = rawMarcToRecord(rawMarc);
    if (record.isPresent()) {
      return getRecordFields(record.get(), tags);
    }
    return "[]";
  }

  public String getFieldsFromMarcJson(String marcJson, String[] tags) {
    Optional<Record> record = marcJsonToRecord(marcJson);
    if (record.isPresent()) {
      return getRecordFields(record.get(), tags);
    }
    return "[]";
  }

  private Optional<Record> marcJsonToRecord(String marcJson) {
    try (InputStream in = new ByteArrayInputStream(marcJson.getBytes())) {
      final MarcJsonReader reader = new MarcJsonReader(in);
      if (reader.hasNext()) {
        return Optional.of(reader.next());
      }
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    } catch (final MarcException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return Optional.empty();
  }

  private Optional<Record> rawMarcToRecord(String rawMarc) {
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes(DEFAULT_CHARSET))) {
      final MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      if (reader.hasNext()) {
        return Optional.of(reader.next());
      }
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    } catch (final MarcException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return Optional.empty();
  }

  private String recordToJson(Record record) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final MarcJsonWriter writer = new MarcJsonWriter(out);
      writer.write(record);
      writer.close();
      return out.toString();
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return "{}";
  }

  private String getRecordFields(Record record, String[] tags) {
    List<VariableField> fields = record.getVariableFields(tags);
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fields);
    } catch (JsonProcessingException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return "[]";
  }

}
