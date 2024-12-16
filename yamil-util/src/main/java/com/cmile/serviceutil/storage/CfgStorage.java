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

package com.cmile.serviceutil.storage;

import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author nishant-pentapalli
 */
@Configuration
@Import({CfgGCPProject.class})
@ComponentScan(basePackages = "com.cmile.serviceutil.storage")
public class CfgStorage {

    @Autowired
    private GCPServiceProject gcpServiceProject;

    @Bean
    public Storage storage() {
        return
                StorageOptions.newBuilder()
                        .setCredentials(gcpServiceProject.getGoogleCredentials())
                        .setProjectId(gcpServiceProject.getProjectId())
                        .build()
                        .getService();
    }
}
