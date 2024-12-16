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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CacheManagerTest {

  private CacheManager<String, String> cacheManager;
  private Function<String, String> loaderFunction;

  @BeforeEach
  void setUp() {
    // Define the loader function
    loaderFunction = key -> "Value for " + key;

    // Initialize CacheManager with 1 minute expiration and maximum size of 10
    cacheManager = new CacheManager<>(1, TimeUnit.MINUTES, 10);
    cacheManager.setLoader(loaderFunction);
  }

  @Test
  void testCacheCreationWithLoader() {
    // Test the creation of cache with a loader function
    String value = cacheManager.getCache("testKey");
    assertEquals("Value for testKey", value);
  }

  @Test
  void testCachePutAndGet() {
    // Put a value into the cache and retrieve it
    cacheManager.putCache("putKey", "putValue");
    String value = cacheManager.getCache("putKey");
    assertEquals("putValue", value);
  }

  @Test
  void testCacheRefresh() {
    // Refresh the cache value
    cacheManager.putCache("refreshKey", "initialValue");
    String valueBeforeRefresh = cacheManager.getCache("refreshKey");
    cacheManager.refreshCache("refreshKey");
    String valueAfterRefresh = cacheManager.getCache("refreshKey");
    assertEquals("Value for refreshKey", valueAfterRefresh);
    // Check if the value was updated correctly after refresh
  }

  @Test
  void testCacheEviction() {
    // Evict a key from the cache
    cacheManager.putCache("evictKey", "evictValue");
    cacheManager.evictCache("evictKey");
    assertDoesNotThrow(() -> cacheManager.getCache("evictKey"));
  }

  @Test
  void testCacheClear() {
    // Clear the entire cache
    cacheManager.putCache("key1", "value1");
    cacheManager.putCache("key2", "value2");

    assertDoesNotThrow(() -> cacheManager.clearCache());
  }

  @Test
  void testLoaderFunctionNotSet() {
    // Test the case where the loader function is not set
    CacheManager<String, String> emptyCacheManager = new CacheManager<>(1, TimeUnit.MINUTES, 10);
    assertThrows(IllegalStateException.class, () -> emptyCacheManager.getCache("key"));
  }
}
