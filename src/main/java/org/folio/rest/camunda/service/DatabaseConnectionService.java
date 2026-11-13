package org.folio.rest.camunda.service;

import static org.folio.spring.tenant.service.SchemaService.SCHEMA_REGEXP;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.folio.rest.camunda.config.FolioDelegateDatabaseConfig;
import org.folio.spring.tenant.config.JpaPropertiesHibernateConfig;
import org.folio.spring.tenant.properties.TenantProperties;
import org.springframework.stereotype.Service;

/**
 * Provide a custom database connections for both FOLIO tenants as well as external connections.
 */
@Service
public class DatabaseConnectionService {

  /**
   * A prefix to any map key for the FOLIO database connections.
   */
  public static final String PREFIX_FOLIO = "folio_";

  /**
   * A prefix to any map key for the non-FOLIO database connections.
   */
  public static final String PREFIX_OTHER = "other_";
  
  /**
   * A key in properties for the connection pool id.
   */
  public static final String POOL_ID = "id";

  /**
   * A key in properties for the connection pool pass word.
   */
  public static final String POOL_PASS = "password";

  /**
   * A key in properties for the connection pool tenant.
   */
  public static final String POOL_TENANT = "tenant";

  /**
   * A key in properties for the connection pool search_path.
   */
  public static final String POOL_SEARCH_PATH = "searchPath";

  /**
   * A key in properties for the connection pool URL.
   */
  public static final String POOL_URL = "url";

  /**
   * A key in properties for the connection pool user name.
   */
  public static final String POOL_USER = "user";

  private final ConcurrentHashMap<String, HikariDataSource> pools;

  private final FolioDelegateDatabaseConfig folioDelegateDatabaseConfig;

  private final JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig;

  private final TenantProperties tenantProperties;

  /**
   * Initializer.
   *
   * @param tenantProperties The Tenant properties configuration.
   */
  public DatabaseConnectionService(FolioDelegateDatabaseConfig folioDelegateDatabaseConfig, JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig, TenantProperties tenantProperties) {

    this.folioDelegateDatabaseConfig = folioDelegateDatabaseConfig;
    this.jpaPropertiesHibernateConfig = jpaPropertiesHibernateConfig;
    this.tenantProperties = tenantProperties;

    this.pools = new ConcurrentHashMap<>();
  }

  /**
   * Create a database connection pool if it does not already exist.
   *
   * @param isFolio TRUE if this is a FOLIO database and requires special handling, FALSE otherwise.
   * @param info All of the remaining parameters.
   *
   * @return The existing or created data source.
   */
  public synchronized HikariDataSource createPool(boolean isFolio, Properties info) {

    return this.pools.computeIfAbsent(buildPoolId(isFolio, info), key -> {
      final HikariConfig config = new HikariConfig();
      final String searchPath = buildSearchPath(isFolio, info, key);

      config.setLeakDetectionThreshold(0);
      config.setJdbcUrl(info.getProperty(POOL_URL));
      config.setUsername(info.getProperty(POOL_USER));
      config.setPassword(info.getProperty(POOL_PASS));

      // config.setDriverClassName();
      config.setConnectionTimeout(folioDelegateDatabaseConfig.getConnectionTimeout());
      config.setIdleTimeout(folioDelegateDatabaseConfig.getIdleTimeout()); 
      config.setMaximumPoolSize(folioDelegateDatabaseConfig.getMaxPoolSize());
      config.setMaxLifetime(folioDelegateDatabaseConfig.getMaxLifetime());
      //config.setDataSourceProperties(datasourceProperties);
      //config.addDataSourceProperty("oracle.jdbc.implicitStatementCacheSize", 10);

      // would be nice to just use this but have to ensure properties match
      // config.setDataSourceProperties(info);

      final HikariDataSource dataSource = new HikariDataSource(config);

      if (!searchPath.isBlank()) {
        dataSource.setConnectionInitSql(String.format("SET search_path TO %s", searchPath));
      }

      return dataSource;
    });
  }

  public synchronized Connection getConnection(String poolKey) throws SQLException {
    return this.pools.get(poolKey).getConnection();
  }

  public synchronized void destroyConnection(String poolKey) throws SQLException {
    // do nothing
  }

  /**
   * Load the pool ID, append the prefix, and fall back to a default tenant if need be.
   *
   * @param isFolio TRUE if this is a FOLIO connection and false otherwise.
   * @param info    An array of all properties.
   *
   * @return The constructed pool ID.
   */
  private String buildPoolId(boolean isFolio, Properties info) {

    final String name;

    if (info.containsKey(POOL_ID)) {
      name = info.getProperty(POOL_ID);
    } else if (isFolio) {
      name = tenantProperties.getDefaultTenant();
    } else {
      name = "";
    }

    if (name.isBlank()) {
      throw new IllegalArgumentException("Database connection pool name must be defined an non-blank.");
    }

    if (!SCHEMA_REGEXP.matcher(name).matches()) {
      throw new IllegalArgumentException("Illegal character in database connection pool name: " + name);
    }

    return isFolio
      ? PREFIX_FOLIO + name
      : PREFIX_OTHER + name;
  }

  /**
   * Build the search path.
   * 
   * This provides the search path as-is, but when isFolio and the path is empty/undefined then the default tenant and schema path is used. 
   *
   * @param isFolio TRUE if this is a FOLIO connection and false otherwise.
   * @param info    An array of all properties.
   * @param name    The pool name (this should be constructed and might be different from info['id']).
   *
   * @return The constructed search path where a blank string represents no custom search path to be set.
   */
  private String buildSearchPath(boolean isFolio, Properties info, String name) {

    String path = info.containsKey(POOL_SEARCH_PATH)
      ? info.getProperty(POOL_SEARCH_PATH)
      : "";

    if (isFolio && path.isBlank()) {
      path = tenantProperties.getDefaultTenant() + "_" + jpaPropertiesHibernateConfig.getDefaultSchema();
    }

    if (!path.isBlank() && !SCHEMA_REGEXP.matcher(path).matches()) {
      throw new IllegalArgumentException("Illegal character in database connection pool '" + name + "' search path name: " + path);
    }

    return path;
  }

}
