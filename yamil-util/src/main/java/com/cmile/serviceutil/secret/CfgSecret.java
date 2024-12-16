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

import com.cmile.serviceutil.common.CfgCommon;
import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author nishant-pentapalli
 */
@Configuration
@Import({CfgCommon.class, CfgGCPProject.class})
@ComponentScan(basePackages = "com.cmile.serviceutil.secret")
public class CfgSecret {

  @Bean
  public SecretManagerServiceClient secretManagerServiceClient(GCPServiceProject gcpServiceProject)
      throws IOException {
    // Create the SecretManagerServiceClient with the credentials
    // Set the credentials provider with the credentials
    SecretManagerServiceSettings settings =
        SecretManagerServiceSettings.newBuilder()
            .setCredentialsProvider(
                FixedCredentialsProvider.create(gcpServiceProject.getGoogleCredentials()))
            .build();

    // Instantiate the SecretManagerServiceClient
    return SecretManagerServiceClient.create(settings);
  }
}
