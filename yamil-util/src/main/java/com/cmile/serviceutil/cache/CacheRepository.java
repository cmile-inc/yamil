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

import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;

public interface CacheRepository<K, V> {
    abstract String getCacheName();

    abstract void hydrateCache(String spaceId);

    abstract MongoTemplate getMongoTemplate();

    abstract Class<V> getEntityClass();

    abstract long getMaxSize();

    abstract K getKey(V entity);

    abstract String getKeyName();

    abstract V getValue(V entity);

    abstract void put(K key, V value) throws Exception;

    abstract Object get(K key) throws Exception;

    abstract void remove(K key) throws Exception;

    abstract long getSize() throws Exception;

    abstract Map<K, V> getAll() throws Exception;
}
