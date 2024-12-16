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

public class DatasourceConstants {

    public static final String HIBERNATE_DIALECT_PROPERTY = "hibernate.dialect";
    public static final String HIBERNATE_MULTI_TENANCY_PROPERTY = "hibernate.multiTenancy";
    public static final String HIBERNATE_DDL_AUTO_PROPERTY = "hibernate.hbm2ddl.auto";
    public static final String HIBERNATE_MULTI_TENANT_CONNECTION_PROVIDER_PROPERTY =
            "hibernate.multi_tenant_connection_provider";
    public static final String HIBERNATE_TENANT_IDENTIFIER_RESOLVER_PROPERTY =
            "hibernate.tenant_identifier_resolver";
    public static final String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default_schema";

    public static final String TEST_ON_BORROW_KEY = "spring.datasource.tomcat.testOnBorrow";
    public static final String VALIDATION_QUERY_KEY = "spring.datasource.tomcat.validationQuery";
    public static final String MULTI_TENANCY = "DATABASE";

    public static final String DEFAULT_SPACE_ID = "defaultSpaceId";
    public static final String NAMING_STRATEGY = "hibernate.physical_naming_strategy";
}
