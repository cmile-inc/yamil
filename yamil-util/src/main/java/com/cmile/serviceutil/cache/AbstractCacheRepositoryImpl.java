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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.serviceutil.metric.MetricsTagName;
import com.cmile.serviceutil.metric.MetricsType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCacheRepositoryImpl<K, V> implements CacheRepository<K, V> {

    private final Cache<K, V> cache;
    private final MetricsService metrics;
    private final MongoTemplate mongoTemplate;
    private final GCPServiceProject gcpServiceProject;
    private final boolean isGlobalScope;

    public AbstractCacheRepositoryImpl(MetricsService metrics, MongoTemplate mongoTemplate,
            GCPServiceProject gcpServiceProject) {
        this.mongoTemplate = mongoTemplate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(getMaxSize())
                .expireAfterWrite(getExpirationTime(), getExpirationUnit())
                .build();
        this.metrics = metrics;
        this.gcpServiceProject = gcpServiceProject;
        this.isGlobalScope = gcpServiceProject.getAppDeploymentScope() != null
                && gcpServiceProject.getAppDeploymentScope().equalsIgnoreCase("global");
    }

    public long getExpirationTime() {
        return 6;
    }

    public TimeUnit getExpirationUnit() {
        return TimeUnit.HOURS;
    }

    @Override
    public void hydrateCache(String spaceId) {

        log.debug("Begin hydrating cache for {}, with spaceId: {}", getCacheName(), spaceId);
        setContext(spaceId);
        metrics.wrapMethodWithMetrics(MetricsType.RESPONSE_TIME.name(), () -> {
            hydrateCacheFromDB();
            return null;
        }, Map.of(MetricsTagName.METHOD_NAME.getTagName(), "cache.hydrate"));

        log.debug("Finished hydrating cache for {}, with number of entries {}", getCacheName(), cache.estimatedSize());
    }

    private void setContext(String spaceId) {
        if (spaceId != null) {
            ContextInfo ctx = new ContextInfo();
            ctx.setDuId(this.gcpServiceProject.getDu());
            ctx.setSpaceId(spaceId);
            ctx.setApplicationName(this.gcpServiceProject.getApplicationName());
            RequestContext.setRequestContextDetails(ctx);
        }
    }

    public void hydrateCacheFromDB() {
        int pageSize = 1000;
        int currentPage = 0;
        boolean hasMoreEntries = true;
        while (hasMoreEntries) {
            Query query = new Query()
                    .skip(currentPage * pageSize) // Skip entries for pagination
                    .limit(pageSize); // Limit the result set to pageSize entries

            // All mongo tables have an _id field, so we can use it for sorting
            // the only way to use pagination in mongo is to sort by a field
            query.with(Sort.by(Sort.Direction.ASC, "_id"));

            List<V> entities = getMongoTemplate().find(query, getEntityClass());

            if (entities.isEmpty()) {
                hasMoreEntries = false; // No more entries left to retrieve
            } else {
                for (V entity : entities) {
                    K key = getSpacedKey(entity);
                    V value = getValue(entity);
                    cache.put(key, value);
                }

                currentPage++; // Move to the next page
            }
        }
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public void put(K key, V value) throws Exception {
        final K spacekey = getSpacedOutKey(key);
        log.debug("Putting {}:{} in {} cache.", getSpacedKeyName(), spacekey, getCacheName());

        metrics.wrapMethodWithMetrics(MetricsType.RESPONSE_TIME.name(), () -> {
            cache.put(spacekey, value);
            return null;
        }, Map.of(MetricsTagName.METHOD_NAME.getTagName(), "cache.put", MetricsTagName.ENTITY_NAME.getTagName(),
                getCacheName()));

    }

    @Override
    public V get(K key) throws Exception {
        final K spacekey = getSpacedOutKey(key);

        log.debug("Querying {} cache for {}: {}", getCacheName(), getSpacedKeyName(), spacekey);

        V value = metrics.wrapMethodWithMetrics(MetricsType.RESPONSE_TIME.name(), () -> {
            return cache.getIfPresent(spacekey);
        }, Map.of(MetricsTagName.METHOD_NAME.getTagName(), "cache.get", MetricsTagName.ENTITY_NAME.getTagName(),
                getCacheName()));
        if (value == null) {
            log.debug(
                    "{}: {} not found in cache having size of: {}", getSpacedKeyName(),
                    spacekey, cache.estimatedSize());
            return null;
        } else {
            log.debug("{} found {} in cache {}: ", getCacheName(), getSpacedKeyName(), spacekey);
        }
        return value;
    }

    @Override
    public void remove(K key) throws Exception {
        final K spacekey = getSpacedOutKey(key);
        log.debug("Removing {}:{} from {} cache.", getSpacedKeyName(), spacekey, getCacheName());
        // The behavior of invalidate() method is undefined if key is not present in the
        // cache. Therefore, we have to first query the cache to see if it is present.
        if (get(spacekey) != null) {
            metrics.wrapMethodWithMetrics(MetricsType.RESPONSE_TIME.name(), () -> {
                cache.invalidate(spacekey);
                return null;
            }, Map.of(MetricsTagName.METHOD_NAME.getTagName(), "cache.remove", MetricsTagName.ENTITY_NAME.getTagName(),
                    getCacheName()));
        }
    }

    @Override
    public long getSize() throws Exception {
        return cache.estimatedSize();
    }

    private K getSpacedKey(V entity) {
        if (!isGlobalScope && RequestContext.getRequestContextDetails() == null) {
            String spaceId = RequestContext.getRequestContextDetails().getSpaceId();
            // K is always a string for us.
            return (K) (spaceId + "|" + getKey(entity));
        } else {
            return getKey(entity);
        }
    }

    private K getSpacedOutKey(K key) {
        if (!isGlobalScope && RequestContext.getRequestContextDetails() == null) {
            String spaceId = RequestContext.getRequestContextDetails().getSpaceId();
            return (K) (spaceId + "|" + key);
        } else {
            return key;
        }
    }

    private String getSpacedKeyName() {
        if (!isGlobalScope && RequestContext.getRequestContextDetails() == null) {
            String spaceId = RequestContext.getRequestContextDetails().getSpaceId();
            return spaceId + "|" + getKeyName();
        } else {
            return getKeyName();
        }
    }

    @Override
    public Map<K, V> getAll() throws Exception {
        return cache.asMap();
    }
}
