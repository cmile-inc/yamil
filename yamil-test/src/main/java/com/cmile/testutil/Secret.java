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

public enum Secret {
    MONGO_ADMIN("mongo_administrator", "testutil/secrets/mongo_admin_secret.json"),
    CUSTOMER_PUBLIC_KEY("customer-public-key", "testutil/secrets/public_key.pem"),
    CUSTOMER_PRIVATE_KEY("customer-private-key", "testutil/secrets/private_key.pem"),
    INTERNAL_PUBLIC_KEY("internal-public-key", "testutil/secrets/public_key.pem"),
    INTERNAL_PRIVATE_KEY("internal-private-key", "testutil/secrets/private_key.pem"),
    SERVICE("service_testutil", "testutil/secrets/service_secret.json"),
    SPACE1("SP01", "testutil/secrets/SP01_secret.json"),
    SPACE2("SP02", "testutil/secrets/SP02_secret.json"),
    SPACE3("SP03", "testutil/secrets/SP03_secret.json"),
    GOOGLE_MAP_KEY("google-map-key", "testutil/secrets/secret_string.txt"),
    OPEN_AI("open-ai-key", "testutil/secrets/open-ai.txt" );
    private final String name;
    private final String secretFile;

    Secret(String name, String secretFile) {
        this.name = name;
        this.secretFile = secretFile;
    }

    public String getSecretName(String projectId) {
        return "projects/" + projectId + "/secrets/" + name + "/versions/latest";
    }

    public String getSecretFile() {
        return secretFile;
    }
}