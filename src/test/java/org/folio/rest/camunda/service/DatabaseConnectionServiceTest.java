package org.folio.rest.camunda.service;

import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_ID;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_PASS;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_URL;
import static org.folio.rest.camunda.service.DatabaseConnectionService.POOL_USER;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.folio.rest.camunda.config.FolioDelegateDatabaseConfig;
import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionServiceTest {

  private static final String KEY = "testPool";
  private static final String URL = "jdbc:h2:mem:testdb";

  private static final String USER = "sa";
  private static final String PASSWORD = "";

  private static final Properties INFO = new Properties();

  private static Server h2Server;

  static {
    INFO.setProperty(POOL_ID, KEY);
    INFO.setProperty(POOL_USER, USER);
    INFO.setProperty(POOL_PASS, PASSWORD);
    INFO.setProperty(POOL_URL, URL);
  }

  @Mock
  private FolioDelegateDatabaseConfig folioDelegateDatabaseConfig;

  @InjectMocks
  private DatabaseConnectionService databaseConnectionService;

  @BeforeAll
  static void startH2() throws SQLException {

    h2Server = Server.createTcpServer().start();

    try (Connection conn = DriverManager.getConnection(URL + ";INIT=CREATE SCHEMA IF NOT EXISTS testdb", USER, PASSWORD)) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE IF NOT EXISTS testdb.test_table (id INT PRIMARY KEY, name VARCHAR(255))");
      }
    }
  }

  @AfterAll
  static void stopH2() {
    h2Server.stop();
  }

  @Test
  void testCreatePool() throws SQLException {
    databaseConnectionService.createPool(false, INFO);
    try (Connection conn = databaseConnectionService.getConnection(KEY)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testCreatePoolAlreadyExists() throws SQLException {
    databaseConnectionService.createPool(false, INFO);
    databaseConnectionService.createPool(false, INFO);
    try (Connection conn = databaseConnectionService.getConnection(KEY)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testGetConnection() throws SQLException {
    assertThrows(NullPointerException.class, () -> {
      databaseConnectionService.getConnection(KEY);
    });
    databaseConnectionService.createPool(false, INFO);
    try (Connection conn = databaseConnectionService.getConnection(KEY)) {
      assertNotNull(conn);
    }
  }

  @Test
  void testDestroyConnection() throws SQLException {
    databaseConnectionService.destroyConnection(KEY);
    assertThrows(NullPointerException.class, () -> {
      databaseConnectionService.getConnection(KEY);
    });
  }
}
