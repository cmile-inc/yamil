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

package com.cmile.serviceutil.mongo;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.testutil.CfgMongoTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {CfgMongoTest.class})
@TestPropertySource(properties = {"spring.spaceId=SP01"})
public class SpaceMongoTemplateProviderTest extends SpaceAbstractCommonTest {
    private static final Logger logger = LoggerFactory.getLogger(SpaceMongoTemplateProviderTest.class);

    @Autowired
    private DynamicMongoTemplate dynamicMongoTemplate;

    @BeforeEach
    public void setUp() {
        setRequestContext();
        logger.debug("Setting up the test");
    }

    @AfterEach
    public void tearDown() {
        logger.debug("Tearing down the test");
    }

    @Test
    public void testSpaceMongoConnection() {
        logger.debug("Testing MongoTemplate with Mocked Setup");
        MongoTemplate mongoTemplate = dynamicMongoTemplate.getMongoTemplate();
        Assertions.assertNotNull(mongoTemplate);
        Assertions.assertEquals(RequestContext.getRequestContextDetails().getSpaceId(), mongoTemplate.getDb().getName());
        mongoTemplate.save(new Document(), "calendar");
        mongoTemplate.find(new Query(), Document.class, "calendar");
    }
}
