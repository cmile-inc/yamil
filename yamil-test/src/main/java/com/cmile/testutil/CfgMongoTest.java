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

package com.cmile.testutil;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.mongo.CfgMongo;
import com.cmile.serviceutil.mongo.DynamicMongoTemplate;
import com.cmile.serviceutil.mongo.MongoCacheManager;
import com.cmile.serviceutil.mongo.MongoTemplateProvider;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import com.cmile.serviceutil.secret.entity.MongoDatabaseEntity;
import com.cmile.serviceutil.secret.entity.ServiceSecretEntity;
import com.cmile.serviceutil.secret.entity.SpaceSecretEntity;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;

/**
 * @author nishant-pentapalli
 */
@Configuration
@Import({CfgSecretTest.class, CfgMongo.class})
public class CfgMongoTest {
    private static final Logger logger = LoggerFactory.getLogger(CfgMongoTest.class);

    @Bean(name = "partiallyMockedMongoAdminEntity")
    @Primary
    public MongoAdminEntity mongoAdminEntity(
            MongoAdminEntity mongoAdminEntity, MongoDBContainer mongoDBContainer) {
        String connectionString = mongoDBContainer.getConnectionString();

        logger.debug("Creating a MongoAdminEntity with connection string: {} ", connectionString);

        MongoAdminEntity mockedMongoAdminEntity = spy(mongoAdminEntity);
        MongoDatabaseEntity mockedMongo = spy(mockedMongoAdminEntity.getMongo());
        when(mockedMongoAdminEntity.getMongo()).thenReturn(mockedMongo);
        when(mockedMongo.getConnectionString()).thenReturn(connectionString);

        return mockedMongoAdminEntity;
    }

    @Bean(name = "partiallyMockedMongoTemplateProvider")
    @Primary
    public MongoTemplateProvider mongoTemplateProvider(
            @Autowired DynamicMongoTemplate dynamicMongoTemplate) {
        MongoTemplateProvider mockedMongoTemplateProvider = spy(dynamicMongoTemplate);
        MongoTemplate mockedMongoTemplate = spy(mockedMongoTemplateProvider.getMongoTemplate());
        when(mockedMongoTemplateProvider.getMongoTemplate()).thenReturn(mockedMongoTemplate);
        return mockedMongoTemplateProvider;
    }

    @Bean(name = "realMongoCacheManager")
    @Primary
    public MongoCacheManager mongoCacheManager(
            GCPServiceProject gcpServiceProject,
            MongoCacheManager mongoCacheManager,
            SecretManagerService realSecretManagerService,
            MongoDBContainer mongoDBContainer) {
        MongoCacheManager realMongoCacheManager = mongoCacheManager;
        // new MongoCacheManager(gcpServiceProject, realSecretManagerService);

        MongoDatabaseEntity mockedMongoDatabaseEntity = null;
        String connectionString = mongoDBContainer.getConnectionString();
        if (ApplicationScopeEnum.GLOBAL
                .name()
                .equalsIgnoreCase(gcpServiceProject.getAppDeploymentScope())) {
            ServiceSecretEntity serviceSecretEntity =
                    spy(
                            (ServiceSecretEntity)
                                    realSecretManagerService.getSecret(SecretTypeEnum.SERVICE, "testutil"));
            when(realSecretManagerService.getSecret(SecretTypeEnum.SERVICE, "testutil"))
                    .thenReturn(serviceSecretEntity);
            mockedMongoDatabaseEntity = spy(serviceSecretEntity.getMongo());

            when(serviceSecretEntity.getMongo()).thenReturn(mockedMongoDatabaseEntity);
            when(mockedMongoDatabaseEntity.getConnectionString())
                    .thenReturn(connectionString + "/" + "testutil");
        } else {
            String spaceId = Optional.ofNullable(RequestContext.getRequestContextDetails())
                    .map(requestContextDetail ->
                            Optional.ofNullable(requestContextDetail.getSpaceId()).orElse("SP01"))
                    .orElse("SP01");
            SpaceSecretEntity spaceSecretEntity =
                    spy(
                            (SpaceSecretEntity)
                                    realSecretManagerService.getSecret(SecretTypeEnum.SPACE, spaceId));
            // Use doReturn to stub the getSecret method and avoid NPE on arguments.
            doReturn(spaceSecretEntity)
                    .when(realSecretManagerService)
                    .getSecret(eq(SecretTypeEnum.SPACE), eq(spaceId));
            mockedMongoDatabaseEntity = spy(spaceSecretEntity.getMongo());
            when(spaceSecretEntity.getMongo()).thenReturn(mockedMongoDatabaseEntity);
            when(mockedMongoDatabaseEntity.getConnectionString())
                    .thenReturn(connectionString + "/" + spaceId);
        }

        return realMongoCacheManager;
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
        mongoDBContainer.start();
        return mongoDBContainer;
    }
}
