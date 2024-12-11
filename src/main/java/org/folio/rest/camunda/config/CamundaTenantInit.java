package org.folio.rest.camunda.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.folio.spring.tenant.hibernate.HibernateTenantInit;
import org.folio.spring.tenant.service.SqlTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CamundaTenantInit implements HibernateTenantInit {

  private static final String SCHEMA_IMPORT_TENANT = "import/tenant";

  private static final String TENANT_TEMPLATE_KEY = "tenant";

  private SqlTemplateService sqlTemplateService;

  @Autowired
  public CamundaTenantInit(SqlTemplateService sqlTemplateService) {
    this.sqlTemplateService = sqlTemplateService;
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
