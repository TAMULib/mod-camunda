package org.folio.rest.camunda.model.enums;

/**
 * A list of supported item types for use in FolioEnvDefaultsItem.
 */
public enum FolioEnvDefaultsItemType {

  /**
   * A literal string value.
   *
   * This is intended to be the standard or generic type.
   */
  LITERAL("literal"),

  /**
   * A value that is to be encrypted in some way when stored in memory.
   *
   * This gets decrypted when needed.
   */
  SECURE("secure"),

  /**
   * A literal string value that includes URL validation and parsing.
   *
   * If the URL is invalid, then this is expected to throw an error on start up.
   *
   * This ensures that the URL does not end in a trailing slash by altering the value as necessary.
   */
  URL("url"),

  /**
   * A literal string value that includes path verification.
   *
   * If the path is invalid, then this is expected to throw an error on start up.
   *
   * This ensures that the path begins with a slash and does not end with a trailing slash by altering the value as necessary.
   */
  URL_PATH("url_path");

  private final String name;

  /**
   * Initialize enum.
   *
   * @param name The name of the field.
   */
  FolioEnvDefaultsItemType(String name) {

    this.name = name;
  }

  /**
   * Get the name.
   *
   * @return The name value.
   */
  public String getName() {

    return name;
  }

}
