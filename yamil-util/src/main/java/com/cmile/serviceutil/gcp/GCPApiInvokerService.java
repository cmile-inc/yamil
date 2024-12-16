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

package com.cmile.serviceutil.gcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class GCPApiInvokerService {
    private static final Logger logger = LoggerFactory.getLogger(GCPApiInvokerService.class);
    private final GoogleCredentials googleCredentials;

    public GCPApiInvokerService(GCPServiceProject serviceProject) {
        this.googleCredentials = serviceProject.getGoogleCredentials();
    }

    public <T> T invokeGcpApi(String apiUrl, String scope, Class<T> clazz) throws IOException {

        // Authenticate using GoogleCredentials
        GoogleCredentials credentials = googleCredentials.createScoped(scope);

        // Create HTTP request factory with credentials
        HttpRequestFactory requestFactory = new NetHttpTransport()
                .createRequestFactory(new HttpCredentialsAdapter(credentials));

        // Build the GET request
        HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(apiUrl));

        // Execute the request
        HttpResponse response = request.execute();

        return response.parseAs(clazz);

    }

   
}
