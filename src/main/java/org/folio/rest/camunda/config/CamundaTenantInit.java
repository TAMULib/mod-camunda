package org.folio.rest.camunda.config;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.folio.spring.tenant.hibernate.HibernateTenantInit;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.service.SqlTemplateService;
import org.springframework.stereotype.Component;

@Component
public class CamundaTenantInit implements HibernateTenantInit {

  private static final String SCHEMA_IMPORT_TENANT = "import/tenant";

  private static final String TENANT_TEMPLATE_KEY = "tenant";

  private static String tenantHeaderName;

  private SqlTemplateService sqlTemplateService;

  private TenantProperties tenantProperties;

  public CamundaTenantInit(SqlTemplateService sqlTemplateService, TenantProperties tenantProperties) {
    this.sqlTemplateService = sqlTemplateService;
    this.tenantProperties = tenantProperties;
  }

  @SuppressWarnings("java:S2696") // SonarQube static assignment from non-static method.
  @PostConstruct
  public void initializeStaticTenantHeader() {
    tenantHeaderName = tenantProperties.getHeaderName();
  }

  @Override
  public void initialize(Connection connection, String tenant) throws SQLException {
    UUID uuid = UUID.randomUUID();
    String id = uuid.toString();
    CamundaTenant camundaTenant = new CamundaTenant(id, 1, tenant);

    try (Statement statement = connection.createStatement()) {
      statement.execute(sqlTemplateService.templateInitSql(SCHEMA_IMPORT_TENANT, TENANT_TEMPLATE_KEY, camundaTenant));
    }
  }

  /**
   * Provide a static way get the tenant header name value.
   *
   * The yaml file names this `tenant.header-name`.
   * The environment variable for this is `TENANT_HEADERNAME`.
   *
   * The OkapiRestTemplate design prevents auto-injection because non-Java code will run the static methods via the MappingUtility.
   * Load the settings in such a way that a static method may access settings injected by Spring.
   *
   * These settings are normally defined in the Spring-Module-Core Spring-Tenant `TenantProperties` class.
   *
   * @return The tenant header.
   */
  public static String getHeaderName() {
    return tenantHeaderName;
  }

  public class CamundaTenant {

    private final String id;

    private final int rev;

    private final String name;

    public CamundaTenant(String id, int rev, String name) {
      this.id = id;
      this.rev = rev;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public int getRev() {
      return rev;
    }

    public String getName() {
      return name;
    }

  }

}
