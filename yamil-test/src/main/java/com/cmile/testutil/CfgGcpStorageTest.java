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

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.utility.DockerImageName;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.aiven.testcontainers.fakegcsserver.FakeGcsServerContainer;

@Configuration
public class CfgGcpStorageTest {

    @Bean
    public FakeGcsServerContainer fakeGscServerContainer() {
        DockerImageName image = DockerImageName.parse("fsouza/fake-gcs-server:1.50.1");
        FakeGcsServerContainer fakeGscServerContainer = new FakeGcsServerContainer(image);
        fakeGscServerContainer.start();
        return fakeGscServerContainer;
    }

    @Bean
    public GoogleCredentials googleCredentials() {
        return Mockito.mock(GoogleCredentials.class);
    }

    @Bean
    @Primary
    public Storage storage(FakeGcsServerContainer fakeGscServerContainer) {
        return Mockito.spy(StorageOptions
                .newBuilder()
                .setHost("http://" + fakeGscServerContainer.getHost() + ":"
                        + fakeGscServerContainer.getFirstMappedPort())
                .build()
                .getService());

    }
}
