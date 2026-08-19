package org.folio.rest.camunda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Provides configuration management for default and reserved variables in Operaton.
 */
@ConfigurationProperties("folio.gateway")
public class FolioGatewayConfig {

  private String loginPath;

  private Long tokenExpireOffset;

  /**
   * Get loginPath.
   *
   * @return The loginPath.
   */
  public String getLoginPath() {

    return loginPath;
  }

  /**
   * Get tokenExpireOffset.
   *
   * @return The tokenExpireOffset.
   */
  public Long getTokenExpireOffset() {

    return tokenExpireOffset;
  }

  /**
   * Set loginPath.
   *
   * @param loginPath The loginPath to set.
   */
  public void setLoginPath(String loginPath) {

    this.loginPath = loginPath;
  }

  /**
   * Set tokenExpireOffset.
   *
   * @param tokenExpireOffset The tokenExpireOffset to set.
   */
  public void setTokenExpireOffset(Long tokenExpireOffset) {

    this.tokenExpireOffset = tokenExpireOffset;
  }

}
