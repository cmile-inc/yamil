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

import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.CfgSecret;
import com.cmile.serviceutil.secret.SecretManagerService;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

/**
 * @author nishant-pentapalli
 */
@Configuration
@Import(CfgSecret.class)
public class CfgSecretTest {
    private static final Logger logger = LoggerFactory.getLogger(CfgSecretTest.class);

    @Bean(name = "realSecretManagerService")
    @Primary
    public SecretManagerService secretManagerService(SecretManagerService secretManagerService)
            throws IOException {
        return spy(secretManagerService);
    }

    @Bean(name = "mockedSecretManagerServiceClient")
    @Primary
    public SecretManagerServiceClient secretManagerServiceClient(GCPServiceProject gcpServiceProject)
            throws IOException {
        logger.debug("Creating a mock SecretManagerServiceClient");

        SecretManagerServiceClient mockedSecretManagerServiceClient =
                mock(SecretManagerServiceClient.class);

        doAnswer(
                invocation -> {
                    AccessSecretVersionRequest request = invocation.getArgument(0);
                    String secretName = request.getName();
                    String projectId = gcpServiceProject.getProjectId();

                    for (Secret secret : Secret.values()) {
                        if (secret.getSecretName(projectId).equals(secretName)) {
                            return mockSecret(secretName, getTestData(secret.getSecretFile()));
                        }
                    }
                    return null; // Return null if no match found
                })
                .when(mockedSecretManagerServiceClient)
                .accessSecretVersion(any(AccessSecretVersionRequest.class));

        return mockedSecretManagerServiceClient;
    }

    private String getTestData(String file) throws IOException {
        // Load the file from the classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);

        // Read the contents of the file into a string
        byte[] privateKeyBytes = inputStream.readAllBytes();
        return new String(privateKeyBytes, StandardCharsets.UTF_8);
    }

    private AccessSecretVersionResponse mockSecret(String secretName, String secretData) {
        ByteString mockData = ByteString.copyFromUtf8(secretData);

        // Create a mocked response for the accessSecretVersion call
        AccessSecretVersionResponse mockResponse =
                AccessSecretVersionResponse.newBuilder()
                        .setName(secretName)
                        .setPayload(
                                com.google.cloud.secretmanager.v1.SecretPayload.newBuilder()
                                        .setData(mockData)
                                        .build())
                        .build();

        return mockResponse;
    }
}
