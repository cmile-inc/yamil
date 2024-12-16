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

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CacheManager<K, V> {

    private final LoadingCache<K, V> cache;
    private Function<K, V> loader;

    public CacheManager(long expirationDuration, TimeUnit timeUnit, long maxSize) {
        // Cache is created but the loader function is set later
        cache =
                Caffeine.newBuilder()
                        .recordStats()
                        .expireAfterAccess(expirationDuration, timeUnit)
                        .maximumSize(maxSize)
                        .build(
                                new CacheLoader<K, V>() {
                                    @Override
                                    public V load(K key) {
                                        if (loader == null) {
                                            throw new IllegalStateException("Loader function is not set.");
                                        }
                                        return loader.apply(key);
                                    }
                                });
    }

    // Method to set the loader after construction
    public void setLoader(Function<K, V> loader) {
        this.loader = loader;
    }

    public V getCache(K key) {
        return cache.get(key);
    }

    public void putCache(K key, V value) {
        cache.put(key, value);
    }

    public V refreshCache(K key) {
        V newValue = loader.apply(key);
        cache.put(key, newValue);
        return newValue;
    }

    public void evictCache(K key) {
        cache.invalidate(key);
    }

    public void clearCache() {
        cache.invalidateAll();
    }
}
