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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = SpaceSecretEntity.Builder.class)
public class SpaceSecretEntity {

  private final String spaceId;
  private final com.cmile.serviceutil.secret.entity.MongoDatabaseEntity mongo;
  private final com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity postgres;

  private SpaceSecretEntity(Builder builder) {
    this.spaceId = builder.spaceId;
    this.mongo = builder.mongo;
    this.postgres = builder.postgres;
  }

  // Getters
  public String getSpaceId() {
    return spaceId;
  }

  public com.cmile.serviceutil.secret.entity.MongoDatabaseEntity getMongo() {
    return mongo;
  }

  public com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity getPostgres() {
    return postgres;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Builder {

    private String spaceId;
    private com.cmile.serviceutil.secret.entity.MongoDatabaseEntity mongo;
    private com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity postgres;

    public Builder() {}

    @JsonProperty("spaceId")
    public Builder spaceId(String spaceId) {
      this.spaceId = spaceId;
      return this;
    }

    @JsonProperty("mongo")
    public Builder mongo(MongoDatabaseEntity mongo) {
      this.mongo = mongo;
      return this;
    }

    @JsonProperty("postgres")
    public Builder postgres(PostgresDatabaseEntity postgres) {
      this.postgres = postgres;
      return this;
    }

    public SpaceSecretEntity build() {
      return new SpaceSecretEntity(this);
    }

    @JsonIgnore
    public Builder fromEntity(SpaceSecretEntity entity) {
      this.spaceId = entity.spaceId;
      this.mongo = entity.mongo;
      this.postgres = entity.postgres;
      return this;
    }
  }
}
