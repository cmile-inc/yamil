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

import com.cmile.serviceutil.cache.CacheManager;
import com.cmile.serviceutil.exception.ConnectionParamsNotFoundException;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.MongoDatabaseEntity;
import com.cmile.serviceutil.secret.entity.ServiceSecretEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class MongoCacheManager extends CacheManager<String, MongoDatabaseEntity> {

  private final GCPServiceProject gcpServiceProject;
  private final SecretManagerService secretManagerService;

  public MongoCacheManager(
      GCPServiceProject gcpServiceProject, SecretManagerService secretManagerService) {
    super(24, TimeUnit.HOURS, 500); // Call the superclass constructor first
    this.gcpServiceProject = gcpServiceProject;
    this.secretManagerService = secretManagerService;
    this.setLoader(this::loadDetails); // Set the loader after the object is initialized
  }

  private MongoDatabaseEntity loadDetails(String id) {
    MongoDatabaseEntity mongoDatabaseEntity = null;

    if (ApplicationScopeEnum.GLOBAL.name().equalsIgnoreCase(gcpServiceProject.getAppDeploymentScope())) {
      ServiceSecretEntity mongoSecrets = this.secretManagerService.getSecret(SecretTypeEnum.SERVICE, id);
      mongoDatabaseEntity = mongoSecrets.getMongo();
    } else {
      SpaceSecretEntity mongoSecrets = this.secretManagerService.getSecret(SecretTypeEnum.SPACE, id);
      mongoDatabaseEntity = mongoSecrets.getMongo();
    }

    if (mongoDatabaseEntity == null) {
      throw new ConnectionParamsNotFoundException(
          String.format(
              "For Service %s or space-id: %s, connection params for MongoDB not found",
              gcpServiceProject.getAppDeploymentScope(), id));
    }

    return mongoDatabaseEntity;
  }
}
