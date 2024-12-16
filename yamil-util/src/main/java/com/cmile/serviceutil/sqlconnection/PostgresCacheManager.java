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

package com.cmile.serviceutil.sqlconnection;

import com.cmile.serviceutil.cache.CacheManager;
import com.cmile.serviceutil.exception.ConnectionParamsNotFoundException;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.PostgresDatabaseEntity;
import com.cmile.serviceutil.secret.entity.ServiceSecretEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PostgresCacheManager extends CacheManager<String, PostgresDatabaseEntity> {

    @Autowired
    private SecretManagerService secretManagerService;
    @Autowired
    private GCPServiceProject gcpServiceProject;

    public PostgresCacheManager() {
        super(5, TimeUnit.DAYS, 500); // Call the superclass constructor first
        this.setLoader(this::loadDetails); // Set the loader after the object is initialized
    }

    private PostgresDatabaseEntity loadDetails(String id) {
        PostgresDatabaseEntity postgresDatabaseEntity = null;
        if (ApplicationScopeEnum.GLOBAL.name().equalsIgnoreCase(gcpServiceProject.getAppDeploymentScope())) {
            ServiceSecretEntity postgresSecret = this.secretManagerService.getSecret(SecretTypeEnum.SERVICE, id);
            postgresDatabaseEntity = postgresSecret.getPostgres();
        } else {
            SpaceSecretEntity postgresSecret = this.secretManagerService.getSecret(SecretTypeEnum.SPACE, id);
            postgresDatabaseEntity = postgresSecret.getPostgres();
        }

        if (postgresDatabaseEntity == null) {
            throw new ConnectionParamsNotFoundException(
                    String.format(
                            "For Service %s or space-id: %s, connection params for PostgresDB not found",
                            gcpServiceProject.getAppDeploymentScope(), id));
        }

        return postgresDatabaseEntity;
    }
}
