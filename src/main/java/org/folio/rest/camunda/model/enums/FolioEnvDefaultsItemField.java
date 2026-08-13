package org.folio.rest.camunda.model.enums;

/**
 * A list of supported fields for use in {@link org.folio.rest.camunda.model.FolioEnvDefaultsItem FolioEnvDefaultsItem}.
 */
public enum FolioEnvDefaultsItemField {

  /**
   * This field controls whether or not the value is exposed (made available) to scripts.
   *
   * If this is not exposed, then only Java (outside of the scripts) has access to this item.
   *
   * The default is expected to be `true`.
   */
  EXPOSE("expose"),

  /**
   * The name of the name field.
   */
  NAME("name"),

  /**
   * The field type.
   */
  TYPE("type"),

  /**
   * The value assigned to the field by default.
   *
   * This is conditionally optional, based on the type.
   */
  VALUE("value");

  private final String name;

  /**
   * Initialize enum.
   *
   * @param name The name of the field.
   */
  FolioEnvDefaultsItemField(String name) {

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
