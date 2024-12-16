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

import com.cmile.serviceutil.sqlconnection.PostgresUtilityColumns;
import com.cmile.testutil.CfgPostgresTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {CfgPostgresTest.class})
@TestPropertySource(properties = {"spring.spaceId=SP01", "spring.jpa.hibernate.ddl-auto=create"})
public class PostgresUtilityColumnsTest extends SpaceAbstractCommonTest {

    @PersistenceContext
    private EntityManager entityManager;

    private TestEntity testEntity;

    @BeforeEach
    public void setUp() {
        testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setCreatedBy("User1");
        testEntity.setUpdatedBy("User1");
    }

    @Test
    @Transactional
    public void testEntityPersistence() {
        entityManager.persist(testEntity);

        assertThat(testEntity.getCreatedBy()).isEqualTo("User1");
        assertThat(testEntity.getUpdatedBy()).isEqualTo("User1");
    }


    @Entity
    @Data
    public static class TestEntity extends PostgresUtilityColumns {

        @Id
        private Long id;
    }
}
