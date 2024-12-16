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

package com.cmile.testutil;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity;
import com.cmile.serviceutil.secret.entity.ServiceSecretEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import com.cmile.serviceutil.sqlconnection.CfgPostgres;
import com.cmile.serviceutil.sqlconnection.PostgresCacheManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.Optional;

import static org.mockito.Mockito.*;

@Configuration
@Import({CfgSecretTest.class, CfgPostgres.class})
@EnableTransactionManagement
public class CfgPostgresTest {

  @Bean
  public PostgreSQLContainer<?> postgresSQLContainer() {
    String dbName =
        Optional.ofNullable(RequestContext.getRequestContextDetails())
            .map(ContextInfo::getSpaceId)
            .filter(spaceId -> !StringUtils.isBlank(spaceId))
            .orElse("testutil");

    PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName(dbName)
            .withUsername("postgres")
            .withPassword("password");
    postgresContainer.start();
    return postgresContainer;
  }

  @Bean(name = "mockPostgresCacheManager")
  @Primary
  public PostgresCacheManager postgresCacheManager(
      GCPServiceProject gcpServiceProject,
      PostgresCacheManager postgresCacheManager,
      SecretManagerService realSecretManagerService,
      PostgreSQLContainer postgreSQLContainer) {
    PostgresCacheManager realMongoCacheManager = postgresCacheManager;

    PostgresDatabaseEntity mockedPostgresDatabaseEntity = null;
    String connectionString = postgreSQLContainer.getJdbcUrl();
    if (ApplicationScopeEnum.GLOBAL
        .name()
        .equalsIgnoreCase(gcpServiceProject.getAppDeploymentScope())) {
      ServiceSecretEntity serviceSecretEntity =
          spy(
              (ServiceSecretEntity)
                  realSecretManagerService.getSecret(SecretTypeEnum.SERVICE, "testutil"));
      when(realSecretManagerService.getSecret(SecretTypeEnum.SERVICE, "testutil"))
          .thenReturn(serviceSecretEntity);
      mockedPostgresDatabaseEntity = spy(serviceSecretEntity.getPostgres());

      when(serviceSecretEntity.getPostgres()).thenReturn(mockedPostgresDatabaseEntity);
      when(mockedPostgresDatabaseEntity.getConnectionString()).thenReturn(connectionString);
      when(mockedPostgresDatabaseEntity.getUsername()).thenReturn("postgres");
      when(mockedPostgresDatabaseEntity.getPassword()).thenReturn("password");
    } else {
      String spaceId =
          Optional.ofNullable(RequestContext.getRequestContextDetails())
              .map(
                  requestContextDetail ->
                      Optional.ofNullable(requestContextDetail.getSpaceId()).orElse("SP01"))
              .orElse("SP01");
      SpaceSecretEntity spaceSecretEntity =
          spy(
              (SpaceSecretEntity)
                  realSecretManagerService.getSecret(SecretTypeEnum.SPACE, spaceId));
      // Use doReturn to stub the getSecret method and avoid NPE on arguments.
      doReturn(spaceSecretEntity)
          .when(realSecretManagerService)
          .getSecret(eq(SecretTypeEnum.SPACE), eq(spaceId));
      mockedPostgresDatabaseEntity = spy(spaceSecretEntity.getPostgres());
      when(spaceSecretEntity.getPostgres()).thenReturn(mockedPostgresDatabaseEntity);
      when(mockedPostgresDatabaseEntity.getConnectionString())
          .thenReturn(connectionString + "/" + spaceId);
      when(mockedPostgresDatabaseEntity.getUsername()).thenReturn("postgres");
      when(mockedPostgresDatabaseEntity.getPassword()).thenReturn("password");
    }

    return realMongoCacheManager;
  }

  @Bean(name = "fakeDatasource")
  @Primary
  public DataSource defaultDataSource(PostgreSQLContainer postgreSQLContainer) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
    hikariConfig.setUsername(postgreSQLContainer.getUsername());
    hikariConfig.setPassword(postgreSQLContainer.getPassword());
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setConnectionTimeout(30000);
    hikariConfig.setIdleTimeout(600000);

    return new HikariDataSource(hikariConfig);
  }
}
