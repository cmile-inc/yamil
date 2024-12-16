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

import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.metric.MetricsService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestCacheRepository extends AbstractCacheRepositoryImpl<String, TestDocument> {

    public TestCacheRepository(MetricsService metrics, MongoTemplate mongoTemplate, GCPServiceProject gcpServiceProject) {
        super(metrics, mongoTemplate, gcpServiceProject);
    }

    @Override
    public String getCacheName() {
        return "TestCache";
    }

    @Override
    public String getKey(TestDocument entity) {
        // Implement logic to derive the key for the entity
        return entity.toString();
    }

    @Override
    public String getKeyName() {
        return "TestKey";
    }

    @Override
    public Class<TestDocument> getEntityClass() {
        return TestDocument.class;
    }

    @Override
    public long getMaxSize() {
        return 10;
    }

    @Override
    public TestDocument getValue(TestDocument entity) {
        return entity;
    }
}
