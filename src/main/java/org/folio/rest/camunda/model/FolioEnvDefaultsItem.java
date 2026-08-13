package org.folio.rest.camunda.model;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.NAME;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.TYPE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField.VALUE;
import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.SECURE;

import jakarta.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.folio.rest.camunda.exception.FolioEnvDefaultsItemFieldException;
import org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.GuardedString.Accessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * A single defaults item for use by {@link org.folio.rest.camunda.config.FolioEnvConfig FolioEnvConfig}.
 */
public class FolioEnvDefaultsItem {

  private static final Logger LOG = LoggerFactory.getLogger(FolioEnvDefaultsItem.class);

  static final String REGX_SLASH_LEAD  = "^/+";
  static final String REGX_SLASH_TRAIL = "/+$";
  static final String REGX_WORD = "[^\\d]\\w+";

  static final String ERR_BAD_FORMAT = "Field named '%s' may only contain word characters without leading digits";
  static final String ERR_NOT_NULL = "Field is required but got NULL";

  static final String LOCALHOST = "http://localhost";

  private Boolean expose;

  private GuardedString guard;

  private String name;

  private FolioEnvDefaultsItemType type;

  private String value;

  /**
   * Initialize class without verification.
   *
   * This is generally only useful for unit tests.
   */
  public FolioEnvDefaultsItem() {
    // Every value should be NULL.
  }

  /**
   * Initialize class.
   *
   * @param lock The lock value (optional).
   * @param name The name value.
   * @param type The type value.
   * @param value The value (optional).
   */
  @ConstructorBinding
  public FolioEnvDefaultsItem(Boolean expose, String name, FolioEnvDefaultsItemType type, String value) {

    this.expose = expose;
    this.guard = null;
    this.name = name;
    this.type = type;

    prepareByType(value);
  }

  /**
   * Get the expose.
   *
   * @return The expose value.
   */
  public Boolean getExpose() {

    return expose;
  }

  /**
   * Get the name.
   *
   * @return The name value.
   */
  public String getName() {

    return name;
  }

  /**
   * Get the string for the given name if and only if it is a SECURE type.
   *
   * This accessor method should be wrapped in a call that clears any local variables via something like SecurityUtil.clear().
   *
   * @param accessor The method used to handle processing the decrypted value.
   *
   * @return TRUE if a secure variable and the accessor has been executed and FALSE otherwise.
   */
  public boolean getSecure(final Accessor accessor) {

    if (!SECURE.equals(type)) {
      LOG.warn("Attempted secure fetch of non-secure FOLIO env defaults item '{}'.", name);
      return false;
    }

    guard.access(accessor);

    return true;
  }

  /**
   * Get the type.
   *
   * @return The type value.
   */
  public FolioEnvDefaultsItemType getType() {

    return type;
  }

  /**
   * Get the value.
   *
   * @return The value value.
   */
  public String getValue() {

    return value;
  }

  @PreDestroy
  public void preDestroy() {

    guard.dispose();
  }

  /**
   * Set the expose.
   *
   * @param expose The value to set.
   */
  public void setExpose(Boolean expose) {

    this.expose = expose;
  }

  /**
   * Set the name.
   *
   * @param name The value to set.
   */
  public void setName(String name) {

    this.name = name;
  }

  /**
   * Set the type.
   *
   * @param type The value to set.
   */
  public void setType(FolioEnvDefaultsItemType type) {

    this.type = type;
  }

  /**
   * Set the value.
   *
   * @param value The value to set.
   */
  public void setValue(String value) {

    this.value = value;
  }

  /**
   * Set the class variables based on the item type.
   *
   * This ensures that variables are only assigned something sensible based on the type.
   *
   * This should be called if the setters are explicitly called to ensure proper data structure.
   *
   * @param value The value to assign to the appropraite location based on the type.
   */
  public void prepareByType(String value) {

    if (name == null) {
      throw new FolioEnvDefaultsItemFieldException(NAME, ERR_NOT_NULL);
    }

    if (!name.matches(REGX_WORD)) {
      throw new FolioEnvDefaultsItemFieldException(NAME, String.format(ERR_BAD_FORMAT, name));
    }

    if (type == null) {
      throw new FolioEnvDefaultsItemFieldException(TYPE, name, ERR_NOT_NULL);
    }

    if (expose == null) {
      expose = true;
    }

    switch (type) {
      case SECURE -> prepareByTypeForSecure(value);

      case URL -> {
        this.value = value;

        prepareByTypeForUrl();
      }

      case URL_PATH -> {
        this.value = value;

        prepareByTypeForUrlPath();
      }

      default -> this.value = value;
    }
  }

  /**
   * Prepare specifically for SECURE type.
   *
   * This stores the data in a different location.
   *
   * @param value The value to store in the guarded string.
   */
  void prepareByTypeForSecure(String value) {

    if (value == null) return;

    guard = new GuardedString(value.toCharArray());
  }

  /**
   * Prepare specifically for URL type.
   *
   * This validates the value as a URL.
   */
  void prepareByTypeForUrl() {

    if (value == null) return;

    try {
      value = new URI(value).toURL().toString();
    } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {
      throw new FolioEnvDefaultsItemFieldException(VALUE, name, e.getMessage(), e);
    }
  }

  /**
   * Prepare specifically for URL_PATH type.
   *
   * This validates the value as a valid path for a URL.
   */
  void prepareByTypeForUrlPath() {

    if (value == null) return;

    try {
      final String fixed = value
        .replaceAll(REGX_SLASH_LEAD, "/")
        .replaceAll(REGX_SLASH_TRAIL, "");

      final String url = (LOCALHOST + fixed);

      new URI(url).toURL();

      value = fixed;
    } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {
      throw new FolioEnvDefaultsItemFieldException(VALUE, name, e.getMessage(), e);
    }
  }

}
