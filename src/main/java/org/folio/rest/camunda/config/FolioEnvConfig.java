package org.folio.rest.camunda.config;

import static org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemType.SECURE;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Provides configuration management for default and reserved variables in Operaton.
 */
@ConfigurationProperties("folio.env")
public class FolioEnvConfig {

  private ConcurrentMap<String, FolioEnvDefaultsItem> defaults = new ConcurrentHashMap<>();

  /**
   * Assign the given item to the defaults structure.
   *
   * @param name  The name, case-sensitive.
   * @param value The new value to assign (May be NULL if allowed by field).
   *
   * @return TRUE on change made and FALSE if no changes made.
   */
  public boolean changeItem(final FolioEnvDefaultsItem item) {

    if (defaults == null || item == null || item.getName() == null) return false;

    return defaults.replace(item.getName(), item) != null;
  }

  /**
   * Change a value for the named item in the defaults.
   *
   * @param name  The name, case-sensitive.
   * @param value The new value to assign (May be NULL if allowed by field).
   *
   * @return TRUE on change made and FALSE if no changes made.
   */
  public boolean changeItemValue(final String name, final String value) {

    if (defaults == null) return false;

    return defaults.computeIfPresent(name, (key, item) -> {
      if (SECURE.equals(item.getType())) {
        item.setSecure(value);
      } else {
        item.setValue(value);
      }

      return item;
    }) != null;
  }

  /**
   * Loop through each item in the defaults.
   *
   * @param action The call back function.
   */
  public void eachItem(BiConsumer<String, FolioEnvDefaultsItem> action) {

    if (defaults != null) {
      defaults.forEach(action);
    }
  }

  /**
   * Get the defaults.
   *
   * @return The defaults value.
   */
  public ConcurrentMap<String, FolioEnvDefaultsItem> getDefaults() {

    return defaults;
  }

  /**
   * Get whether or not defaults exists.
   *
   * @return TRUE if defaults is not NULL and FALSE otherwise.
   */
  public boolean hasDefaults() {

    return defaults != null;
  }

  /**
   * Assign the given item to the defaults only if the item does not exist.
   *
   * @param value The new item to assign, if one does not already exist.
   *
   * @return TRUE on change made and FALSE if no changes made.
   */
  public boolean initializeItem(final FolioEnvDefaultsItem item) {

    if (defaults == null || item == null || item.getName() == null) return false;

    defaults.computeIfAbsent(item.getName(), key -> item);

    return true;
  }

  /**
   * Get the named item for the named item in the defaults.
   *
   * @param name The name to get, case-sensitive.
   *
   * @return The found item or NULL on not found.
   */
  public FolioEnvDefaultsItem retrieveItem(final String name) {

    if (defaults == null) return null;

    return defaults.get(name);
  }

  /**
   * Get the value for the named item for the named item in the defaults.
   *
   * @param name The name to get, case-sensitive.
   *
   * @return The found item or NULL on not found.
   */
  public String retrieveItemValue(final String name) {

    if (defaults == null) return null;

    final FolioEnvDefaultsItem found = defaults.get(name);

    if (found == null) return null;

    return found.getValue();
  }

  /**
   * Set the defaults.
   *
   * @param defaults The value to set.
   */
  public void setDefaults(final ConcurrentMap<String, FolioEnvDefaultsItem> defaults) {

    this.defaults = defaults;
  }

}
