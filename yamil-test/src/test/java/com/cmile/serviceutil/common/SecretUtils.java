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

package com.cmile.serviceutil.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import com.cmile.serviceutil.secret.entity.MongoDatabaseEntity;
import com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;

public class SecretUtils {

  public static SecretManagerService mockSecretManagerService() {
    SecretManagerService secretManagerService = mock(SecretManagerService.class);

    try {
      MongoAdminEntity mae =
          MongoAdminEntity.builder()
              .privateKey("142951b9-9014-4c67-ab9c-1064a190f824")
              .publicKey("vexvqegi")
              .projectId("66c8983bd2698905844a1608")
              .mongo(
                  MongoDatabaseEntity.builder()
                      .username("admin")
                      .password("password")
                      .domain("cluster0.5q2jv.mongodb.net")
                      .database("cmile-test")
                      .appName("admin")
                      .build())
              .build();

      when(secretManagerService.getSecret(SecretTypeEnum.MONGO_ADMIN, null)).thenReturn(mae);

      SpaceSecretEntity spaceSecretEntity =
          SpaceSecretEntity.builder()
              .spaceId("spaceId")
              .postgres(
                  PostgresDatabaseEntity.builder()
                      .database("spaceId")
                      .host("localhost")
                      .password("password")
                      .port(5432)
                      .username("username")
                      .build())
              .mongo(
                  MongoDatabaseEntity.builder()
                      .database("spaceId")
                      .password("password")
                      .username("username")
                      .build())
              .build();

      when(secretManagerService.getSecret(SecretTypeEnum.SPACE, "spaceId"))
          .thenReturn(spaceSecretEntity);

    } catch (Exception error) {
      error.printStackTrace();
    }
    return secretManagerService;
  }
}
