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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.cmile.serviceutil.secret.entity.MongoDatabaseEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class SecretEntitiesTest {

  @Test
  public void testEntitySerialization() {
    ObjectMapper objectMapper = new ObjectMapper();
    String mongoDb =
        "{\"username\":\"test-user\",\"password\":\"test-password\",\"domain\":\"test-domain\",\"database\":\"test-database\",\"appName\":\"test-app\"}";
    try {
      MongoDatabaseEntity obj =
          objectMapper.readValue(mongoDb, MongoDatabaseEntity.class);
      assertEquals("test-user", obj.getUsername(), "Username should be test-user");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    String secretValue =
        "{\"spaceId\":\"test-space\",\"mongo\":{\"username\":\"test-user\",\"password\":\"test-password\",\"domain\":\"test-domain\",\"database\":\"test-database\",\"appName\":\"test-app\"}}";
    try {
      SpaceSecretEntity obj =
          objectMapper.readValue(secretValue, SpaceSecretEntity.class);
      assertEquals("test-space", obj.getSpaceId(), "Space ID should be test-space");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
