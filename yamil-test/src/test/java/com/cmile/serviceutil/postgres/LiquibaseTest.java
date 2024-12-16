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

package com.cmile.serviceutil.postgres;

import com.cmile.serviceutil.sqlconnection.migration.LiquibaseService;
import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgPostgresTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {CfgPostgresTest.class})
public class LiquibaseTest extends SpaceAbstractCommonTest {

    @Autowired
    private LiquibaseService liquibaseService;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    @Transactional
    public void runMigration() {
        liquibaseService.runMigrations();
        String checkTableSql = "SELECT table_name FROM information_schema.tables where table_name='tenants'";

        int tableCount = entityManager.createNativeQuery(checkTableSql).getResultList().size();

        // Assert that the table has been created
        Assertions.assertEquals(1L, tableCount, "The table 'user_global' should exist.");

    }

}
