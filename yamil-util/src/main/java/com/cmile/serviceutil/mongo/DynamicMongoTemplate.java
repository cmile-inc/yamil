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
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.entity.MongoDatabaseEntity;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Component
public class DynamicMongoTemplate implements MongoTemplateProvider {

  public static final String DEFAULT_SPACE_ID = "dummy";
  public static final String DEFAULT_MONGO_CON_STRING = "mongodb://dummy";
  private static final Logger logger = LoggerFactory.getLogger(DynamicMongoTemplate.class);
  private final ConcurrentMap<String, MongoTemplate> mongoTemplates = new ConcurrentHashMap<>();

  private final MongoCacheManager mongoCacheManager;
  private final GCPServiceProject gcpServiceProject;

  public DynamicMongoTemplate(
      MongoCacheManager mongoCacheManager, GCPServiceProject gcpServiceProject) {
    this.mongoCacheManager = mongoCacheManager;
    this.gcpServiceProject = gcpServiceProject;
  }

  @Override
  public MongoTemplate getMongoTemplate() {
      String key = getMongoCacheKey();
    if (key == null
        || DEFAULT_SPACE_ID.equals(key)) {
      logger.info("Getting MongoTemplate for SpaceId/Service: {}", key);
      return buildMongoTemplate(DEFAULT_MONGO_CON_STRING, key);
    }
    logger.info("Getting MongoTemplate for SpaceId/Service: {}", key);
    return mongoTemplates.computeIfAbsent(
        key,
        id -> {
          MongoDatabaseEntity databaseEntity = mongoCacheManager.getCache(key);
          return buildMongoTemplate(databaseEntity.getConnectionString(), databaseEntity.getDatabase());
        });
  }

  private MongoTemplate buildMongoTemplate(String connectionString, String databaseName) {

    MongoClientSettings mongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .applyToConnectionPoolSettings(
                builder ->
                    builder.maxConnectionIdleTime(60L, TimeUnit.SECONDS).minSize(1).maxSize(15))
            .build();
    return new org.springframework.data.mongodb.core.MongoTemplate(
        MongoClients.create(mongoClientSettings), databaseName);
  }

  private String getMongoCacheKey() {
    if (ApplicationScopeEnum.GLOBAL.name().equalsIgnoreCase(
        gcpServiceProject.getAppDeploymentScope())) {
        return gcpServiceProject.getApplicationName().split("-")[1];
    } else {
      return Optional.ofNullable(RequestContext.getRequestContextDetails())
          .map(
              requestContextDetail ->
                  Optional.ofNullable(requestContextDetail.getSpaceId()).orElse(DEFAULT_SPACE_ID))
          .orElse(DEFAULT_SPACE_ID);
    }
  }
}
