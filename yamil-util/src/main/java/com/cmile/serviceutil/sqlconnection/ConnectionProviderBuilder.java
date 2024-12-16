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

import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import java.util.HashMap;

public class ConnectionProviderBuilder {
    private final HashMap<String, Object> properties = new HashMap<>();

    public ConnectionProviderBuilder withUrl(String dbUrl) {
        properties.put("hibernate.connection.url", dbUrl);
        return this;
    }

    public ConnectionProviderBuilder withSchema(String schema) {
        properties.put(DatasourceConstants.HIBERNATE_DEFAULT_SCHEMA, schema);
        return this;
    }
    public ConnectionProviderBuilder withDriverClass(String driverClassName) {
        properties.put("hibernate.connection.driver_class", driverClassName);
        return this;
    }

    public ConnectionProviderBuilder withMultiTenancy(String multiTenancyMode) {
        properties.put("spring.jpa.properties.hibernate.multiTenancy", multiTenancyMode);
        return this;
    }

    public ConnectionProviderBuilder withDialect(String dialect) {
        properties.put("spring.jpa.properties.hibernate.dialect", dialect);
        return this;
    }

    public ConnectionProviderBuilder withDdlAuto(String ddlAuto) {
        properties.put("spring.jpa.properties.hibernate.ddl-auto", ddlAuto);
        return this;
    }

    public ConnectionProviderBuilder withAutoCommit(boolean autoCommit) {
        properties.put("hibernate.connection.autocommit", Boolean.toString(autoCommit));
        return this;
    }

    public ConnectionProviderBuilder withCredentials(String username, String password) {
        properties.put("hibernate.connection.username", username);
        properties.put("hibernate.connection.password", password);
        return this;
    }

    public ConnectionProviderBuilder withShowSql(boolean showSql) {
        properties.put("spring.jpa.properties.hibernate.show_sql", Boolean.toString(showSql));
        return this;
    }

    public ConnectionProviderBuilder withNamingStrategy(String strategy) {
//        properties.put("spring.jpa.hibernate.naming.physical-strategy", strategy);
        properties.put("hibernate.physical_naming_strategy", strategy);
        return this;
    }

    public ConnectionProvider build() {
        DriverManagerConnectionProviderImpl connectionProvider =
                new DriverManagerConnectionProviderImpl();
        connectionProvider.configure(properties);
        return connectionProvider;
    }
}
