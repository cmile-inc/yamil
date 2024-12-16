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

@JsonDeserialize(builder = ServiceSecretEntity.Builder.class)
public class ServiceSecretEntity {

    private final String service;
    private final com.cmile.serviceutil.secret.entity.MongoDatabaseEntity mongo;
    private final com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity postgres;

    private ServiceSecretEntity(Builder builder) {
        this.service = builder.service;
        this.mongo = builder.mongo;
        this.postgres = builder.postgres;
    }

    // Getters
    public String getService() {
        return service;
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

        private String service;
        private com.cmile.serviceutil.secret.entity.MongoDatabaseEntity mongo;
        private com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity postgres;

        public Builder() {
        }

        @JsonProperty("service")
        public Builder service(String service) {
            this.service = service;
            return this;
        }

        @JsonProperty("mongo")
        public Builder mongo(MongoDatabaseEntity mongo) {
            this.mongo = mongo;
            return this;
        }

        @JsonProperty("postgres")
        public ServiceSecretEntity.Builder postgres(PostgresDatabaseEntity postgres) {
            this.postgres = postgres;
            return this;
        }

        public ServiceSecretEntity build() {
            return new ServiceSecretEntity(this);
        }

        @JsonIgnore
        public Builder fromEntity(ServiceSecretEntity entity) {
            this.service = entity.service;
            this.mongo = entity.mongo;
            this.postgres = entity.postgres;
            return this;
        }
    }
}
