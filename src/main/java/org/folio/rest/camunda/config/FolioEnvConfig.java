package org.folio.rest.camunda.config;

import java.util.List;
import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Provides configuration management for default and reserved variables in Operaton.
 */
@ConfigurationProperties("folio.env")
public class FolioEnvConfig {

  private List<FolioEnvDefaultsItem> defaults;

  /**
   * Get the defaults.
   *
   * @return The defaults value.
   */
  public List<FolioEnvDefaultsItem> getDefaults() {

    return defaults;
  }

  /**
   * Set the defaults.
   *
   * @param defaults The value to set.
   */
  public void setDefaults(List<FolioEnvDefaultsItem> defaults) {

    this.defaults = defaults;
  }

}
