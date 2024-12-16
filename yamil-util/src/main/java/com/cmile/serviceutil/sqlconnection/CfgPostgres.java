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

package com.cmile.serviceutil.sqlconnection;

import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.CfgSecret;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.sqlconnection.migration.LiquibaseService;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ComponentScan(value = "com.cmile.serviceutil.sqlconnection")
@Import({ CfgGCPProject.class, CfgSecret.class })
public class CfgPostgres {

    @Autowired
    private final DatasourceProperties datasourceProperties;

    public CfgPostgres(DatasourceProperties datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }

    // Can enable in future if default datasourse is set
    // @Bean
    // public DataSource defaultDataSource() {
    // Define default datasource here and assign
    // TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();
    // Map<Object, Object> dataSources = new HashMap<>();
    // // Set up tenant-specific datasources
    // routingDataSource.setTargetDataSources(dataSources);
    // return routingDataSource;
    // }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider multiTenantConnectionProviderImpl,
            CurrentTenantIdentifierResolverImpl currentTenantIdentifierResolverImpl,
            GCPServiceProject gcpServiceProject) {
                if (gcpServiceProject.getAppDeploymentScope().equals(ApplicationScopeEnum.GLOBAL.name())) {
                        return new EntityManagerFactoryBuilder()
                        .withPackagesToScan("com.cmile.*")
                        .withDataSource(dataSource)
                        .withJpaVendorAdapter(jpaVendorAdapter())
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_DDL_AUTO_PROPERTY, datasourceProperties.getDdlAuto())
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_DIALECT_PROPERTY, datasourceProperties.getDialect())
                        .withHibernateProperty(DatasourceConstants.HIBERNATE_DEFAULT_SCHEMA,
                                datasourceProperties.getDbSchema().orElse("public"))
                        .withHibernateProperty(DatasourceConstants.NAMING_STRATEGY, datasourceProperties.getHibernateNamingStrategy())
                        .withTestOnBorrow(true)
                        .withValidationQuery("select 1")
                        .build();
                } else {
                        return new EntityManagerFactoryBuilder()
                        .withPackagesToScan("com.cmile.*")
                        .withDataSource(dataSource)
                        .withJpaVendorAdapter(jpaVendorAdapter())
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_DDL_AUTO_PROPERTY, datasourceProperties.getDdlAuto())
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_DIALECT_PROPERTY, datasourceProperties.getDialect())
                        .withHibernateProperty(DatasourceConstants.HIBERNATE_DEFAULT_SCHEMA,
                                datasourceProperties.getDbSchema().orElse("public"))
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_MULTI_TENANCY_PROPERTY, DatasourceConstants.MULTI_TENANCY)
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_MULTI_TENANT_CONNECTION_PROVIDER_PROPERTY,
                                multiTenantConnectionProviderImpl)
                        .withHibernateProperty(
                                DatasourceConstants.HIBERNATE_TENANT_IDENTIFIER_RESOLVER_PROPERTY,
                                currentTenantIdentifierResolverImpl)
                        .withHibernateProperty(DatasourceConstants.NAMING_STRATEGY, datasourceProperties.getHibernateNamingStrategy())
                        .withTestOnBorrow(true)
                        .withValidationQuery("select 1")
                        .build();
                }
        
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setShouldRun(false); // Disable Liquibase
        return liquibase;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}
