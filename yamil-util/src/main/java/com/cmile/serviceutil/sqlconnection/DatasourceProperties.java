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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
@Configuration
public class DatasourceProperties {

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String dialect;

    @Value("${spring.jpa.properties.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Value("${spring.datasource.driverClassName:org.postgresql.Driver}")
    private String driverClass;

    @Value(
            "${spring.jpa.hibernate.naming.physical-strategy:org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy}")
    private String springNamingStrategy;

    @Value(
            "${hibernate.physical_naming_strategy:org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy}")
    private String hibernateNamingStrategy;

    @Value("${spring.datasource.url:null}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:null}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:null}")
    private String datasourcePassword;
    
    @Value("${spring.datasource.dbSchema:null}")
    private String dbSchema;

    public Optional<String> getDbSchema() {
        if (dbSchema.equals("null")) {
            return Optional.empty();
        }
        return Optional.ofNullable(dbSchema);
    }

    public Optional<String> getDatasourceUrl() {
        if (datasourceUrl.equals("null")) {
            return Optional.empty();
        }
        return Optional.ofNullable(datasourceUrl);
    }

    public Optional<String> getDatasourceUsername() {
        if (datasourceUsername.equals("null")) {
            return Optional.empty();
        }
        return Optional.ofNullable(datasourceUsername);
    }

    public Optional<String> getDatasourcePassword() {
        if (datasourcePassword.equals("null")) {
            return Optional.empty();
        }
        return Optional.ofNullable(datasourcePassword);
    }

    public String getSpringNamingStrategy() {
        return springNamingStrategy;
    }

    public String getHibernateNamingStrategy() {
        return hibernateNamingStrategy;
    }


    public String getDialect() {
        return dialect;
    }

    public String getDdlAuto() {
        return ddlAuto;
    }

    public String getDriverClass() {
        return driverClass;
    }
    
}
