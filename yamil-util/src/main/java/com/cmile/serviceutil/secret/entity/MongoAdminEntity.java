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

package com.cmile.serviceutil.secret.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = MongoAdminEntity.Builder.class)
public class MongoAdminEntity {

  private final String privateKey;
  private final String publicKey;
  private final String projectId;
  private final MongoDatabaseEntity mongo;

  private MongoAdminEntity(Builder builder) {
    this.privateKey = builder.privateKey;
    this.publicKey = builder.publicKey;
    this.projectId = builder.projectId;
    this.mongo = builder.mongo;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean isEmpty() {
    return privateKey == null || publicKey == null || projectId == null || mongo.isEmpty();
  }

  // Getters

  public String getPrivateKey() {
    return privateKey;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public String getProjectId() {
    return projectId;
  }

  public MongoDatabaseEntity getMongo() {
    return mongo;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Builder {

    private String privateKey;
    private String publicKey;
    private String projectId;
    private MongoDatabaseEntity mongo;

    public Builder() {}

    @JsonProperty("privateKey")
    public Builder privateKey(String privateKey) {
      this.privateKey = privateKey;
      return this;
    }

    @JsonProperty("publicKey")
    public Builder publicKey(String publicKey) {
      this.publicKey = publicKey;
      return this;
    }

    @JsonProperty("projectId")
    public Builder projectId(String projectId) {
      this.projectId = projectId;
      return this;
    }

    @JsonProperty("mongo")
    public Builder mongo(MongoDatabaseEntity mongo) {
      this.mongo = mongo;
      return this;
    }

    public MongoAdminEntity build() {
      return new MongoAdminEntity(this);
    }
  }
}
