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

import com.cmile.serviceutil.common.CfgCommon;
import com.cmile.serviceutil.secret.CfgSecret;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CfgCommon.class, CfgSecret.class})
@ComponentScan(value = "com.cmile.serviceutil.mongo")
public class CfgMongo {

  @Bean
  public MongoAdminEntity mongoAdminEntity(SecretManagerService secretManagerService) {
    return secretManagerService.getSecret(SecretTypeEnum.MONGO_ADMIN, null);
  }
}
