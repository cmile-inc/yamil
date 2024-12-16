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

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider<String> {
    private static final Logger logger = LoggerFactory.getLogger(MultiTenantConnectionProvider.class);
    private final PostgresCacheManager postgresCacheManager;
    private final GCPServiceProject gcpServiceProject;
    private final DatasourceProperties datasourceProperties;

    // Map to hold connection providers for each tenant
    private final Map<String, ConnectionProvider> connectionProviders = new ConcurrentHashMap<>();

    public MultiTenantConnectionProvider(
            PostgresCacheManager postgresCacheManager,
            GCPServiceProject gcpServiceProject,
            DatasourceProperties datasourceProperties) {
        this.postgresCacheManager = postgresCacheManager;
        this.gcpServiceProject = gcpServiceProject;
        this.datasourceProperties = datasourceProperties;
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return Optional.ofNullable(RequestContext.getRequestContextDetails())
                .map(ContextInfo::getSpaceId)
                .map(this::getConnectionProvider)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Cannot create new connection provider on startup as no tenant available"));
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return getConnectionProvider(tenantIdentifier);
    }

    private ConnectionProvider getConnectionProvider(String tenantIdentifier) {
        // Check the cache first
        return connectionProviders.computeIfAbsent(tenantIdentifier, id -> {
            ConnectionProvider connectionProvider = loadConnectionProviderDetails(id);
            return Optional.ofNullable(connectionProvider)
                    .orElseThrow(() -> new RuntimeException(
                            String.format("Cannot create new connection provider for tenant: %s", tenantIdentifier)));
        });
    }

    private ConnectionProvider loadConnectionProviderDetails(String id) {
        PostgresDatabaseEntity postgresDatabaseEntity = this.postgresCacheManager.getCache(id);
        logger.info("Connection creation for id {}", datasourceProperties.getDatasourceUrl().orElse(postgresDatabaseEntity.getConnectionString()), id);
        return new ConnectionProviderBuilder()
                .withUrl(datasourceProperties.getDatasourceUrl().orElse(postgresDatabaseEntity.getConnectionString()))
                .withSchema(datasourceProperties.getDbSchema().orElse("public"))
                .withDriverClass(datasourceProperties.getDriverClass())
                .withMultiTenancy(DatasourceConstants.MULTI_TENANCY)
                .withDialect(datasourceProperties.getDialect())
                .withDdlAuto(datasourceProperties.getDdlAuto())
                .withAutoCommit(true)
                .withCredentials(datasourceProperties.getDatasourceUsername().orElse(postgresDatabaseEntity.getUsername()), datasourceProperties.getDatasourcePassword().orElse(postgresDatabaseEntity.getPassword()))
                .withShowSql(true)
                .withNamingStrategy(datasourceProperties.getHibernateNamingStrategy())
                .build();
    }
}
