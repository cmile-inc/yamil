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

import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgPostgresTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author nishant-pentapalli
 */
@SpringBootTest(classes = {CfgPostgresTest.class})
public class GlobalPostgresConnectionTest extends AbstractCommonTest {
    private static final Logger logger = LoggerFactory.getLogger(GlobalPostgresConnectionTest.class);
    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        logger.debug("Tearing down the test");
    }

    @Test
    @Transactional
    public void testGlobalConnectionProvider() {
        logger.debug("Testing Global connection provider");

        // SQL to create the table
        String createTableSql = "CREATE TABLE IF NOT EXISTS user_global (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL" +
                ")";

        // Execute the SQL command
        entityManager.createNativeQuery(createTableSql).executeUpdate();
        entityManager.flush();
        // Verify if the table exists (Optional)
        String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'user_global'";

        Long tableCount = (Long) entityManager.createNativeQuery(checkTableSql).getSingleResult();

        // Assert that the table has been created
        Assertions.assertEquals(1L, tableCount, "The table 'user_global' should exist.");
    }
}
