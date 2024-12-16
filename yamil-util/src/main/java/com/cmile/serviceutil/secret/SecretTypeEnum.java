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

package com.cmile.serviceutil.secret;

import com.cmile.serviceutil.common.json.JsonEntityMapper;
import com.cmile.serviceutil.common.util.ThrowingBiFunction;
import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import com.cmile.serviceutil.secret.entity.ServiceSecretEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import java.io.IOException;

public enum SecretTypeEnum {
  MONGO_ADMIN(
      "mongo-admin", (mapper, secret) -> mapper.readJsonToEntity(secret, MongoAdminEntity.class)) {
    @Override
    public String getSecretId(String secret) {
      return "mongo_administrator";
    }
  },
  SPACE("space", (mapper, secret) -> mapper.readJsonToEntity(secret, SpaceSecretEntity.class)) {
    @Override
    public String getSecretId(String secret) {
      return secret;
    }
  },
  SERVICE(
      "service", (mapper, secret) -> mapper.readJsonToEntity(secret, ServiceSecretEntity.class)) {
    @Override
    public String getSecretId(String secret) {
      return "service_" + secret;
    }
  },
  INTERNAL_PUBLIC_KEY("internal-public-key", (mapper, secret) -> secret) {
    @Override
    public String getSecretId(String secret) {
      return "internal-public-key";
    }
  },
  INTERNAL_PRIVATE_KEY("internal-private-key", (mapper, secret) -> secret) {
    @Override
    public String getSecretId(String secret) {
      return "internal-private-key";
    }
  },
  CUSTOMER_PUBLIC_KEY("customer-public-key", (mapper, secret) -> secret) {
    @Override
    public String getSecretId(String secret) {
      return "customer-public-key";
    }
  },
  CUSTOMER_PRIVATE_KEY("customer-private-key", (mapper, secret) -> secret) {
    @Override
    public String getSecretId(String secret) {
      return "customer-private-key";
    }
  },
  OPEN_AI_KEY("open-ai-key", (mapper, secret) -> secret) {
    @Override
    public String getSecretId(String secret) {
      return "open-ai-key";
    }
  },
  GOOGLE_MAP_KEY("google-map-key", (mapper, secret) -> secret) {
    @Override
    public String getSecretId(String secret) {
      return "google-map-key";
    }
  };

  private final String value;
  private final ThrowingBiFunction<JsonEntityMapper, String, ?, IOException> deserializer;

  SecretTypeEnum(
      String value, ThrowingBiFunction<JsonEntityMapper, String, ?, IOException> deserializer) {
    this.value = value;
    this.deserializer = deserializer;
  }

  public abstract String getSecretId(String secret);

  @SuppressWarnings("unchecked") // Suppress unchecked cast warning
  public final <T> T getSecretEntity(JsonEntityMapper mapper, String secret) throws IOException {
    return (T) this.deserializer.apply(mapper, secret);
  }

  public String getValue() {
    return value;
  }
}
