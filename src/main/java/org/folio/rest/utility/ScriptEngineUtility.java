package org.folio.rest.utility;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.marc4j.MarcException;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;

/**
 * A collection of utilities methods intended to be directly used by scripts
 * called by the scripting engine.
 */
public class ScriptEngineUtility {

  private static final String EMAIL_REGEX = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
  private static final String PHONE_REGEX = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
  private static final String URL_REGEX = "(http(s)?:\\\\\\/\\\\\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\\\\\+~#=]{2,256}\\\\\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\\\\\+.~#?&//=]*)";

  private static final Pattern EMAIL_PATTERN = compile(EMAIL_REGEX, CASE_INSENSITIVE | MULTILINE);
  private static final Pattern PHONE_PATTERN = compile(PHONE_REGEX, CASE_INSENSITIVE | MULTILINE);
  private static final Pattern URL_PATTERN = compile(URL_REGEX, CASE_INSENSITIVE | MULTILINE);

  /**
   * Check if a given string is a proper e-mail address.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isEmail(String string) {
    return EMAIL_PATTERN.matcher(string).find();
  }

  /**
   * Check if a given string might be an e-mail.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isEmailLike(String string) {
    return isEmail(string) || (string == null ? false : string.toLowerCase().indexOf("@") != -1);
  }

  /**
   * Check if a given string is a phone number.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isPhone(String string) {
    return PHONE_PATTERN.matcher(string).find();
  }

  /**
   * Check if a given string might be a URL.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isURLLike(String string) {
    if (string == null) {
      return false;
    }

    boolean isLikeUrl = string.toLowerCase().indexOf("www.") != -1;

    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".org") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".edu") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".net") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".us") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".io") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".co") != -1;

    return (isValidUrl(string) || isLikeUrl) && !isEmailLike(string);
  }

  /**
   * Check if a given string is a valid URL.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isValidUrl(String string) {
    if (string == null) {
      return false;
    }

    return URL_PATTERN.matcher(string).find();
  }

  /**
   * Creating JSONObject.
   *
   * @return A generated JSON object.
   */
  public JSONObject createJson() {
    return new JSONObject();
  }

  /**
   * Decode a JSON string into a JSONObject.
   *
   * @param json The JSON string to decode.
   *
   * @return A generated JSON object, containing the decoded JSON string.
   */
  public JSONObject decodeJson(String json) {
    return new JSONObject(json);
  }

  /**
   * Encode a JSONObject into a JSON string.
   *
   * @param json The JSONObject to encode.
   *
   * @return A String containing the encoded JSON data.
   */
  public String encodeJson(JSONObject json) {
    if (json == null) {
      return "{}";
    }

    return json.toString(2);
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
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes())) {
      final MarcStreamReader reader = new MarcStreamReader(in);
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        final MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        while (reader.hasNext()) {
          Record record = reader.next();
          writer.write(record);
        }
        writer.close();
        return out.toString();
      }
    } catch (final IOException e) {
      // TODO: do something in case of exception
    } catch (final MarcException e) {
      // TODO: do something in case of exception
    }
    return "{}";
  }

}