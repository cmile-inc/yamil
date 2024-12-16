/*
 * Copyright 2024 cmile inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmile.serviceutil.sqlconnection.migration;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity;
import com.cmile.serviceutil.secret.entity.ServiceSecretEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import com.cmile.serviceutil.sqlconnection.DatasourceProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LiquibaseService {

  // Static final variables for property keys and literals
  private static final String LIQUIBASE_CHANGELOG_PATH = "classpath:db/db.changelog-master.yaml";
  private static final String CREATE_SCHEMA_SQL_TEMPLATE = "CREATE SCHEMA IF NOT EXISTS \"%s\"";

  @Autowired
  private ResourceLoader resourceLoader;
  @Autowired
  private DatasourceProperties datasourceProperties;

  @Value("${spring.datasource.driverClassName:org.postgresql.Driver}")
  private String driverClassName;

  private final GCPServiceProject gcpServiceProject;
  private final SecretManagerService secretManagerService;

  public LiquibaseService(
      GCPServiceProject gcpServiceProject, SecretManagerService secretManagerService) {
    this.gcpServiceProject = gcpServiceProject;
    this.secretManagerService = secretManagerService;
  }

  public void runMigrations() {
    String schemaName = datasourceProperties.getDbSchema().orElse(gcpServiceProject.getApplicationName().replace("service-", ""));
    DataSource dataSource = createDataSource(schemaName);
    try (Connection connection = dataSource.getConnection()) {

      // Check and create schema if not exists
      createSchemaIfNotExists(connection, schemaName); // Schema name from prop file

      connection.setSchema(schemaName); // Set schema for the connection
      log.info("Schema set to: {}", schemaName);

      // Create and configure Liquibase
      SpringLiquibase liquibase = createLiquibase(dataSource, schemaName);
      liquibase.afterPropertiesSet(); // Run migrations
      log.info("Successfully ran Liquibase migrations for schema: {}", schemaName);

    } catch (SQLException e) {
      log.error("SQL error running Liquibase migrations for schema: {}", schemaName, e);
    } catch (Exception e) {
      log.error("Error running Liquibase migrations for schema: {}", schemaName, e);
    }
  }

  private DataSource createDataSource(String schemaName) {

    PostgresDatabaseEntity postgresDatabaseEntity = null;
    if (ApplicationScopeEnum.GLOBAL.name().equalsIgnoreCase(gcpServiceProject.getAppDeploymentScope())) {
      ServiceSecretEntity postgresSecret = this.secretManagerService.getSecret(SecretTypeEnum.SERVICE, gcpServiceProject.getApplicationName().replace("service-", ""));
      postgresDatabaseEntity = Optional.ofNullable(postgresSecret.getPostgres()).orElseThrow(
          () -> new RuntimeException("Postgres secret is not available for Global"));
    } else {
      ContextInfo requestContextDetail = Optional.ofNullable(RequestContext.getRequestContextDetails())
          .orElseThrow(() -> new RuntimeException("Request context details are not available"));

      String spaceId = Optional.ofNullable(requestContextDetail.getSpaceId())
          .orElseThrow(() -> new RuntimeException("Request Context Space ID is not available"));

      SpaceSecretEntity spaceSecret = 
          (SpaceSecretEntity)
              Optional.ofNullable(this.secretManagerService.getSecret(SecretTypeEnum.SPACE, spaceId))
          .orElseThrow(
                    () -> 
                        new RuntimeException(
                  "Space secret is not available for space ID: " + spaceId));

      postgresDatabaseEntity = Optional.ofNullable(spaceSecret.getPostgres())
          .orElseThrow(
                () -> 
                    new RuntimeException(
                  "Postgres secret is not available for space ID: " + spaceId));
    }

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(datasourceProperties.getDatasourceUrl().orElse(postgresDatabaseEntity.getConnectionString()));
    dataSource.setSchema(schemaName);
    dataSource.setUsername(datasourceProperties.getDatasourceUsername().orElse(postgresDatabaseEntity.getUsername()));
    dataSource.setPassword(datasourceProperties.getDatasourcePassword().orElse(postgresDatabaseEntity.getPassword()));
    return dataSource;
  }

  private SpringLiquibase createLiquibase(DataSource dataSource, String schema) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(LIQUIBASE_CHANGELOG_PATH); // Use constant path
    liquibase.setResourceLoader(resourceLoader);
    // Set schema-specific property if needed (depends on Liquibase setup)
    liquibase.setDefaultSchema(schema);
    return liquibase;
  }

  private void createSchemaIfNotExists(Connection connection, String schemaName)
      throws SQLException {
    String sql = String.format(CREATE_SCHEMA_SQL_TEMPLATE, schemaName); // Use constant SQL template
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
      log.info("Schema '{}' created or already exists.", schemaName);
    } catch (SQLException e) {
      log.error("Error creating schema '{}': {}", schemaName, e.getMessage());
      throw e;
    }
  }
}
