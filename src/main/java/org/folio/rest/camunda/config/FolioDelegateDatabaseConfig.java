package org.folio.rest.camunda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Provide standard configuration settings for delegate related database connections.
 */
@Component
@ConfigurationProperties(prefix = "folio.delegate.database")
public class FolioDelegateDatabaseConfig {

  private Long connectionTimeout;

  private Long idleTimeout;

  private Long maxLifetime;

  private Integer maxPoolSize;

  /**
   * Get the connection time out value.
   *
   * @return The value.
   */
  public Long getConnectionTimeout() {

    return connectionTimeout;
  }

  /**
   * Get the idle time out value.
   *
   * @return The value.
   */
  public Long getIdleTimeout() {

    return idleTimeout;
  }

  /**
   * Get the maximum life time value.
   *
   * @return The value.
   */
  public Long getMaxLifetime() {

    return maxLifetime;
  }

  /**
   * Get the maximum pool size value.
   *
   * @return The value.
   */
  public Integer getMaxPoolSize() {

    return maxPoolSize;
  }

  /**
   * Set connection time out value.
   *
   * @param connectionTimeout The new value.
   */
  public void setConnectionTimeout(Long connectionTimeout) {

    this.connectionTimeout = connectionTimeout;
  }

  /**
   * Set idle time out value.
   *
   * @param idleTimeout The new value.
   */
  public void setIdleTimeout(Long idleTimeout) {

    this.idleTimeout = idleTimeout;
  }

  /**
   * Set maximum life time value.
   *
   * @param maxLifetime The new value.
   */
  public void setMaxPoolSize(Long maxLifetime) {

    this.maxLifetime = maxLifetime;
  }

  /**
   * Set maximum pool size value.
   *
   * @param maxPoolSize The new value.
   */
  public void setMaxPoolSize(Integer maxPoolSize) {

    this.maxPoolSize = maxPoolSize;
  }

}
