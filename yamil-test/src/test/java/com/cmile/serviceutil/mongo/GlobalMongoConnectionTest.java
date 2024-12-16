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

import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgMongoTest;
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


/**
 * @author nishant-pentapalli
 */
@SpringBootTest(classes = {CfgMongoTest.class})
public class GlobalMongoConnectionTest extends AbstractCommonTest {
    private static final Logger logger = LoggerFactory.getLogger(GlobalMongoConnectionTest.class);
    @Autowired
    private DynamicMongoTemplate dynamicMongoTemplate;

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        logger.debug("Tearing down the test");
    }

    @Test
    public void testGlobalConnectionProvider() {
        logger.debug("Testing Global connection provider");
        MongoTemplate mongoTemplate = dynamicMongoTemplate.getMongoTemplate();
        Assertions.assertNotNull(dynamicMongoTemplate.getMongoTemplate());
        Assertions.assertEquals("cmile-test", mongoTemplate.getDb().getName());
        mongoTemplate.save(new Document(), "calendar");
        mongoTemplate.find(new Query(), Document.class, "calendar");
    }
}
