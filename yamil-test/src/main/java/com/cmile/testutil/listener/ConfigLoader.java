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

package com.cmile.testutil.lisener;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import org.springframework.core.env.Environment;

import java.util.UUID;

public class ConfigLoader {

    public static void loadConfigurations(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name");
        String applicationScope = environment.getProperty("spring.application.scope");
        String spaceId = environment.getProperty("spring.spaceId");
        String du = environment.getProperty("spring.du");

        ContextInfo contextInfo;
        ApplicationScopeEnum serviceType = ApplicationScopeEnum.valueOf(applicationScope);
        switch (serviceType) {
            case GLOBAL:
                contextInfo = new ContextInfo();
                contextInfo.setSpaceId(applicationName.split("-")[1]);
                contextInfo.setApplicationName(applicationName);
                contextInfo.setCorrelationId(UUID.randomUUID().toString());
                RequestContext.setRequestContextDetails(contextInfo);
                break;
            case DU:
                contextInfo = new ContextInfo();
                contextInfo.setSpaceId(spaceId == null ? "SP01" : spaceId);
                contextInfo.setDuId(du);
                contextInfo.setApplicationName(applicationName);
                contextInfo.setCorrelationId(UUID.randomUUID().toString());
                RequestContext.setRequestContextDetails(contextInfo);
                break;
            default:
                throw new IllegalArgumentException("Unknown service type: " + serviceType);
        }
    }
}
