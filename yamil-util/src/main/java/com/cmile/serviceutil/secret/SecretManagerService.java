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

import com.cmile.serviceutil.common.json.JsonEntityMapper;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.GetSecretRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.UpdateSecretRequest;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import java.io.Closeable;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class SecretManagerService implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(SecretManagerService.class);

  private final String projectId;

  private final JsonEntityMapper jsonEntityMapper;
  private final SecretManagerServiceClient secretManagerServiceClient;

  public SecretManagerService(
      GCPServiceProject gcpServiceProject,
      SecretManagerServiceClient secretManagerServiceClient,
      JsonEntityMapper jsonEntityMapper)
      throws IOException {
    logger.debug(
        "Initializing SecretManagerService for project: {}", gcpServiceProject.getProjectId());
    this.projectId = gcpServiceProject.getProjectId();
    this.jsonEntityMapper = jsonEntityMapper;

    this.secretManagerServiceClient = secretManagerServiceClient;
  }

  public <T> T getSecret(SecretTypeEnum secretType, String secretId) {
    try {
      // Retrieve the secret value as a JSON string
      String secretValue = getMostRecentSecretVersion(secretType.getSecretId(secretId));

      // Deserialize the JSON string to a SpaceSecretEntity object
      return secretType.getSecretEntity(jsonEntityMapper, secretValue);

    } catch (ApiException e) {
      // If the secret does not exist, return null
      if (e.getStatusCode().getCode() == StatusCode.Code.NOT_FOUND) {
        return null;
      }
      logger.error("Failed to get secret: {}", secretId, e);
      throw e;
    } catch (IOException e) {
      logger.error("Failed to get secret: {}", secretId, e);
      throw new RuntimeException(e);
    }
  }

  public void saveSecret(SecretTypeEnum secretType, Object secretEntity, String secretId)
      throws IOException {
    // Serialize SpaceSecretEntity to JSON
    String jsonString = jsonEntityMapper.writeEntityToJson(secretEntity);

    // Create SecretPayload with JSON string
    SecretPayload payload =
        SecretPayload.newBuilder().setData(ByteString.copyFromUtf8(jsonString)).build();

    // Save the secret
    setSecretVersion(secretType.getSecretId(secretId), payload);
  }

  @Cacheable("secrets")
  public String getMostRecentSecretVersion(String secretId) throws IOException {
    // Build the secret name with the latest version
    String secretName =
        String.format("projects/%s/secrets/%s/versions/latest", projectId, secretId);

    // Build the request to access the secret version
    AccessSecretVersionRequest request =
        AccessSecretVersionRequest.newBuilder().setName(secretName).build();

    // Access the secret version
    AccessSecretVersionResponse response = secretManagerServiceClient.accessSecretVersion(request);

    // Get the secret payload
    ByteString secretData = response.getPayload().getData();

    // Return the secret data as a string
    return secretData.toStringUtf8();
  }

  @CacheEvict(value = "secrets", key = "#secretId")
  private void setSecretVersion(String secretId, SecretPayload payload) {
    SecretName secretName = SecretName.of(projectId, secretId);
    // Try to create the secret
    ProjectName project = ProjectName.of(projectId);

    Secret secret =
        Secret.newBuilder()
            .setReplication(
                Replication.newBuilder()
                    .setAutomatic(Replication.Automatic.newBuilder().build())
                    .build())
            .build();
    try {
      secretManagerServiceClient.createSecret(project, secretId, secret);
    } catch (ApiException e) {
      // If the secret already exists, do nothing
      if (e.getStatusCode().getCode() != StatusCode.Code.ALREADY_EXISTS) {
        throw e;
      } else {
        if (isSecretMarkedForDeletion(secretName)) {
          undeleteSecret(secretId);
        }
      }
    }

    // Add the secret payload.
    secretManagerServiceClient.addSecretVersion(secretName, payload);

    logger.info("Created secret: {}", secretName);
  }

  @CacheEvict(value = "secrets", key = "#secretId")
  public void deleteSecret(String secretId) {
    SecretName secretName = SecretName.of(projectId, secretId);
    DeleteSecretRequest request =
        DeleteSecretRequest.newBuilder().setName(secretName.toString()).build();
    try {
      secretManagerServiceClient.deleteSecret(request);
    } catch (ApiException e) {
      // If the secret does not exist, do nothing
      if (e.getStatusCode().getCode() != StatusCode.Code.NOT_FOUND) {
        throw e;
      }
    }
  }

  public void undeleteSecret(String secretId) {
    SecretName secretName = SecretName.of(projectId, secretId);
    UpdateSecretRequest request =
        UpdateSecretRequest.newBuilder()
            .setSecret(Secret.newBuilder().setName(secretName.toString()).build())
            .setUpdateMask(FieldMask.newBuilder().addPaths("expire_time"))
            .build();
    secretManagerServiceClient.updateSecret(request);
    logger.info("Undeleted secret: {}", secretName);
  }

  public boolean isSecretMarkedForDeletion(SecretName secretName) {
    GetSecretRequest request = GetSecretRequest.newBuilder().setName(secretName.toString()).build();
    Secret secret = secretManagerServiceClient.getSecret(request);
    Timestamp expireTime = secret.getExpireTime();
    return expireTime != null;
  }

  @Override
  public void close() {
    secretManagerServiceClient.close();
  }
}
