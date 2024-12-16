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

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPApiInvokerService;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.storage.CfgStorage;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.HashMap;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Import({CfgStorage.class, CfgGCPProject.class})
@TestPropertySource(properties = {
        "gcp.storage.base-url=http://localhost:${FAKE_GCS_PORT}"
})
public class CfgStorageTest {

    @Autowired
    GCPServiceProject gcpServiceProject;

    @Bean("fakeGCSStorageContainer")
    @Primary
    public Storage gcsStorageContainer() {
        GenericContainer<?> fakeGcsServer = new GenericContainer<>(DockerImageName.parse("fsouza/fake-gcs-server:latest"))
                .withExposedPorts(4443) // Fake GCS server default port
                .withEnv("STORAGE_EMULATOR_HOST", "localhost:4443")
                .withCommand("-scheme http");

        fakeGcsServer.start();
        System.setProperty("gcp.storage.base-url", "http://" + fakeGcsServer.getHost() + ":" + fakeGcsServer.getMappedPort(4443));

        return spy(StorageOptions.newBuilder()
                .setHost( "http://" + fakeGcsServer.getHost() + ":" + fakeGcsServer.getMappedPort(4443))   // Points to the fake GCS server
                .setProjectId(gcpServiceProject.getProjectId())
                .build()
                .getService());
    }

    @Bean
    @Primary
    public GCPApiInvokerService gcpApiInvokerService() {
        GCPApiInvokerService gcpApiInvokerService = Mockito.mock(GCPApiInvokerService.class);
         try {
            Mockito.when(gcpApiInvokerService.invokeGcpApi(any(), any(), any())).thenReturn(new HashMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcpApiInvokerService;
    }


}
