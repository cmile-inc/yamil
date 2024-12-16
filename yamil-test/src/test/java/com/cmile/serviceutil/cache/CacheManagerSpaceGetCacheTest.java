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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheManagerSpaceGetCacheTest {

    private CacheManager<String, String> cacheManager;

    @BeforeEach
    public void setUp() {
        cacheManager = new CacheManager<>(1, TimeUnit.MINUTES, 10);
    }

    @Test
    void testSetLoader() {
        Function<String, String> loader = key -> "Value: " + key;
        cacheManager.setLoader(loader);
        String value = cacheManager.getCache("TestKey");
        assertEquals("Value: TestKey", value);
    }

    @Test
    void testGetCache_WithLoaderNotSet() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cacheManager.getCache("TestKey");
        });
        assertEquals("Loader function is not set.", exception.getMessage());
    }

    @Test
    void testPutCache() {
        Function<String, String> loader = key -> "Value: " + key;
        cacheManager.setLoader(loader);
        cacheManager.putCache("TestKey", "StoredValue");
        String value = cacheManager.getCache("TestKey");
        assertEquals("StoredValue", value);
    }

    @Test
    void testRefreshCache() {
        Function<String, String> loader = key -> "Value: " + key;
        cacheManager.setLoader(loader);
        cacheManager.putCache("TestKey", "OldValue");
        String newValue = cacheManager.refreshCache("TestKey");
        assertEquals("Value: TestKey", newValue);
        assertEquals("Value: TestKey", cacheManager.getCache("TestKey"));
    }

    @Test
    void testEvictCache() {
        Function<String, String> loader = key -> "Value: " + key;
        cacheManager.setLoader(loader);
        cacheManager.putCache("TestKey", "StoredValue");
        cacheManager.evictCache("TestKey");
    }

    @Test
    void testClearCache() {
        Function<String, String> loader = key -> "Value: " + key;
        cacheManager.setLoader(loader);
        cacheManager.putCache("TestKey", "StoredValue");
        cacheManager.clearCache();
    }

}
