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

import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

public class EntityManagerFactoryBuilder {

  private final HashMap<String, Object> properties = new HashMap<>();
  private String[] packagesToScan;
  private DataSource dataSource;
  private JpaVendorAdapter jpaVendorAdapter;

  public EntityManagerFactoryBuilder withPackagesToScan(String... packagesToScan) {
    this.packagesToScan = packagesToScan;
    return this;
  }

  public EntityManagerFactoryBuilder withDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public EntityManagerFactoryBuilder withJpaVendorAdapter(JpaVendorAdapter jpaVendorAdapter) {
    this.jpaVendorAdapter = jpaVendorAdapter;
    return this;
  }

  public EntityManagerFactoryBuilder withHibernateProperty(String key, Object value) {
    properties.put(key, value);
    return this;
  }

  public EntityManagerFactoryBuilder withTestOnBorrow(boolean testOnBorrow) {
    properties.put(DatasourceConstants.TEST_ON_BORROW_KEY, testOnBorrow);
    return this;
  }

  public EntityManagerFactoryBuilder withValidationQuery(String validationQuery) {
    properties.put(DatasourceConstants.VALIDATION_QUERY_KEY, validationQuery);
    return this;
  }

  public LocalContainerEntityManagerFactoryBean build() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setPackagesToScan(packagesToScan);
    em.setDataSource(dataSource);
    em.setJpaVendorAdapter(jpaVendorAdapter);
    em.setJpaPropertyMap(properties);
    return em;
  }
}
