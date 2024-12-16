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

package com.cmile.serviceutil.cache;


import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.serviceutil.mongo.DynamicMongoTemplate;
import com.cmile.testutil.CfgMongoTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {CfgMongoTest.class, CfgGCPProject.class})
public class AbstractCacheRepositoryImplTest extends SpaceAbstractCommonTest {

    @Autowired
    private DynamicMongoTemplate mongoTemplate;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private GCPServiceProject gcpServiceProject;

    private TestCacheRepository testCacheRepository;

    @BeforeEach
    public void setUp() {
        testCacheRepository = new TestCacheRepository(metricsService, mongoTemplate.getMongoTemplate(), gcpServiceProject);
    }

    @Test
    public void testCacheHydrationFromDatabase() throws Exception {
        mongoTemplate.getMongoTemplate().save(new TestDocument());

        testCacheRepository.hydrateCache("testSpaceId");

        Map<String, TestDocument> cacheEntries = testCacheRepository.getAll();
        assertFalse(cacheEntries.isEmpty(), "Cache should be hydrated with DB entries");
    }

    @Test
    public void testPutAndGetFromCache() throws Exception {
        String key = "testKey";
        TestDocument value = new TestDocument();

        // Insert value into the cache
        testCacheRepository.put(key, new TestDocument());

        // Retrieve value from the cache and assert correctness
        Object retrievedValue = testCacheRepository.get(key);
        assertNotNull(retrievedValue, "Value should be present in cache");
    }

    @Test
    public void testRemoveFromCache() throws Exception {
        String key = "testKey";
        TestDocument value = new TestDocument();


        // Insert value into the cache
        testCacheRepository.put(key, value);

        // Remove the value from the cache
        testCacheRepository.remove(key);

        // Attempt to retrieve and verify it's no longer present
        Object retrievedValue = testCacheRepository.get(key);
        assertNull(retrievedValue, "Value should be removed from cache");
    }

    @Test
    public void testCacheSize() throws Exception {

        // Add multiple entries to the cache
        testCacheRepository.put("key1", new TestDocument());
        testCacheRepository.put("key2", new TestDocument());

        // Verify cache size
        long size = testCacheRepository.getSize();
        assertEquals(2, size, "Cache size should match number of inserted entries");
    }


}
