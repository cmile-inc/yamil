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

import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AtlasMongoDbService {

  private final AtlasMongoApiClient digestApi;
  private final MongoAdminEntity mongoAdminEntity;
  private MongoClient mongoClient;
  private String mongo_projectId;

  @Value("${mongodb.atlas.base.url:https://cloud.mongodb.com/api/atlas/v2/groups/}")
  private String atlasBaseUrl;

  public AtlasMongoDbService(MongoAdminEntity mongoAdminEntity, AtlasMongoApiClient digestApi)
      throws NullPointerException {
    this.mongoAdminEntity = mongoAdminEntity;
    this.digestApi = digestApi;
  }

  @PostConstruct
  public void init() {
    this.mongo_projectId = mongoAdminEntity.getProjectId();

    MongoClientSettings mongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(
                new ConnectionString(mongoAdminEntity.getMongo().getConnectionString()))
            .build();

    this.mongoClient = MongoClients.create(mongoClientSettings);
  }

  public void createCollection(String dbName, String collectionName) {
    MongoDatabase database = mongoClient.getDatabase(dbName);
    database.createCollection(collectionName);
  }

  public void dropCollection(String dbName, String collectionName) {
    MongoDatabase database = mongoClient.getDatabase(dbName);
    database.getCollection(collectionName).drop();
  }

  public void dropDatabase(String dbName) {
    mongoClient.getDatabase(dbName).drop();
  }

  public void createUserWithDatabaseAccess(String dbName, String username, String password)
      throws RuntimeException {
    if (digestApi == null) {
      throw new RuntimeException("Remote access token not initialized");
    }
    JsonObject request = new JsonObject();
    request.addProperty("databaseName", "admin");
    request.addProperty("groupId", this.mongo_projectId);
    request.addProperty("username", username);
    request.addProperty("password", password);

    JsonArray roles = new JsonArray();
    JsonObject role = new JsonObject();
    role.addProperty("databaseName", dbName);
    role.addProperty("roleName", "readWrite");
    roles.add(role);
    request.add("roles", roles);
    log.debug("Creating user with database access: " + request);
    Mono<String> response =
        digestApi.postApi(
            atlasBaseUrl + this.mongo_projectId + "/databaseUsers?envelope=false&pretty=false",
            request.toString());

    log.info(response.block(Duration.ofSeconds(10)));
  }

  public void deleteUser(String username) throws RuntimeException {
    if (digestApi == null) {
      throw new RuntimeException("Remote access token not initialized");
    }
    Mono<String> response =
        digestApi.deleteApi(
            atlasBaseUrl + this.mongo_projectId + "/databaseUsers/admin/" + username);
    log.info(response.block(Duration.ofSeconds(10)));
  }

  public boolean userExists(String username) {
    if (digestApi == null) {
      throw new RuntimeException("Remote access token not initialized");
    }
    Mono<String> response =
        digestApi.getApi(atlasBaseUrl + this.mongo_projectId + "/databaseUsers/admin/" + username);
    try {
      String r = response.block(Duration.ofSeconds(10));
      return r != null && !r.isEmpty();
    } catch (Exception e) {
      return false;
    }
  }
}
