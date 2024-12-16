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

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.GCPApiInvokerService;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.gcp.GcpSpacesNamingStrategyUtils;
import com.google.cloud.storage.*;
import com.google.cloud.storage.NotificationInfo.EventType;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GcpCloudStorageService {
  private static final Logger logger = LoggerFactory.getLogger(GcpCloudStorageService.class);

  @Autowired
  private GCPServiceProject gcpServiceProject;

  @Autowired
  private GCPApiInvokerService gcpApiInvokerService;

  private final Storage storage;

  public GcpCloudStorageService(Storage storage) {
    this.storage = storage;
  }

  public boolean hasBucket(String bucketName) throws StorageException {
    return storage.get(bucketName) != null;
  }

  public void setCorsOnBucket(String bucketName) {
    Bucket bucket = storage.get(bucketName);
    String responseHeader = "Content-Type";
    Integer maxAgeSeconds = 3600;
    Cors cors =
        Cors.newBuilder()
            .setOrigins(ImmutableList.of(Cors.Origin.of("*")))
            .setMethods(ImmutableList.of(HttpMethod.PUT, HttpMethod.GET))
            .setResponseHeaders(ImmutableList.of(responseHeader))
            .setMaxAgeSeconds(maxAgeSeconds)
            .build();

    bucket.toBuilder().setCors(ImmutableList.of(cors)).build().update();
}

  public void createBucketIfNeeded(String bucketName, String region) throws IllegalArgumentException {
    if (bucketName == null) {
      throw new IllegalArgumentException("Bucket name cannot be null");
    }
    if (storage.get(bucketName) == null) {
      storage.create(BucketInfo.newBuilder(bucketName).setLocation(region).build());     
    }
    try {
      setCorsOnBucket(bucketName);
    } catch (StorageException e) {
      logger.error("Failed to create bucket: " + bucketName);
    }

  }

  public List<Map<String, Object>> listBucketNotifications(String bucketName) throws IOException {
    String apiUrl = String.format("https://storage.googleapis.com/storage/v1/b/%s/notificationConfigs", bucketName);
    String scope = "https://www.googleapis.com/auth/cloud-platform";
    Map<String, Object> responseData = gcpApiInvokerService.invokeGcpApi(apiUrl, scope, Map.class);
    List<Map<String, Object>> notifications = (List<Map<String, Object>>) responseData.get("items");
    return notifications;
  }

  public void assignTopicToBucket(String bucketName, String topicId, EventType[] eventTypes,
      Map<String, String> attributes) throws StorageException, IOException {

    NotificationInfo notificationInfo = NotificationInfo
        .newBuilder("projects/" + gcpServiceProject.getProjectId() + "/topics/" + topicId)
        .setEventTypes(eventTypes)
        .setEtag("etag")
        .setCustomAttributes(attributes)
        .build();

    List<Map<String, Object>> existingNotifications = null;
    try {
      existingNotifications = this.listBucketNotifications(bucketName);
    } catch (Exception e) {
      logger.error("Failed to list bucket notifications: " + e.getMessage());
    }
    if (existingNotifications == null || existingNotifications.size() == 0) {
      storage.createNotification(bucketName, notificationInfo);
    }
  }

  public void destroyBucket(String bucketName) throws StorageException {
    // Get the bucket
    Bucket bucket = storage.get(bucketName);
    if (bucket == null) {
      logger.error("Bucket not found: " + bucketName);
      return;
    }

    // List and delete all objects in the bucket
    for (Blob blob : bucket.list().iterateAll()) {
      storage.delete(blob.getBlobId());
      logger.debug("Deleted object: " + blob.getName());
    }

    // Delete the bucket
    boolean deleted = storage.delete(bucketName);
    if (deleted) {
      logger.debug("Deleted bucket: " + bucketName);
    } else {
      logger.error("Failed to delete bucket: " + bucketName);
    }

  }

  public GCPCloudStoragePath uploadFile(File file) throws IOException {
    ContextInfo requestContextDetail = RequestContext.getRequestContextDetails();
    String spaceId = requestContextDetail.getSpaceId();
    String bucketName = GcpSpacesNamingStrategyUtils.bucketName(spaceId);
    return uploadFile(file, bucketName);
  }

  public GCPCloudStoragePath uploadFile(String filepath, String contents) throws IOException {
    ContextInfo requestContextDetail = RequestContext.getRequestContextDetails();
    String spaceId = requestContextDetail.getSpaceId();
    String bucketName = GcpSpacesNamingStrategyUtils.bucketName(spaceId);
    return uploadFile(filepath, contents, bucketName);
  }

  public URL getSignedUrl(String bucketName, StoragePathEnum filePath, String fileName, String contentType)
      throws RuntimeException {

    if (!hasBucket(bucketName)) {
      throw new RuntimeException("Bucket does not exist");
    }

    // Concatenate filePath and fileName using Linux file paths
    String fullPath = Paths.get(filePath.getPath(), fileName).toString();

    // Define BlobId
    BlobId blobId = BlobId.of(bucketName, fullPath);

    // Define BlobInfo
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    // Generate Signed URL
    Map<String, String> extensionHeaders = new HashMap<>();
    extensionHeaders.put("Content-Type", contentType);

    URL signedUrl = storage.signUrl(
        blobInfo,
        15,
        TimeUnit.MINUTES,
        SignUrlOption.httpMethod(HttpMethod.PUT),
        SignUrlOption.withExtHeaders(extensionHeaders),
        SignUrlOption.withV4Signature());

    // Return the signed URL
    return signedUrl;
  }

  public String moveFile(String bucketName, String objectId, StoragePathEnum originalPath, StoragePathEnum newPath) {
    // Create the BlobId for the source file
    BlobId sourceBlobId = BlobId.of(bucketName, objectId);

    // Create the BlobId for the destination file
    String newObjectId = objectId.replaceAll(originalPath.getPath(), newPath.getPath());
    BlobId targetBlobId = BlobId.of(bucketName, newObjectId);

    // Rename (move) the file
    storage.copy(
        Storage.CopyRequest.newBuilder().setSource(sourceBlobId).setTarget(targetBlobId).build());

    // Delete the original file
    storage.delete(sourceBlobId);

    return newObjectId;
  }

  public String readFile(String bucketName, String filePath) throws StorageException {
    Blob blob = storage.get(bucketName, filePath);
    return new String(blob.getContent());
  }

  public GCPCloudStoragePath uploadFile(File file, String bucketName) throws RuntimeException {
    try {
      BlobId blobId = BlobId.of(bucketName, file.getPath());
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

      Storage.BlobWriteOption precondition;
      if (storage.get(bucketName, file.getPath()) == null) {
        precondition = Storage.BlobWriteOption.doesNotExist();
      } else {
        precondition = Storage.BlobWriteOption.generationMatch(
            storage.get(bucketName, file.getPath()).getGeneration());
      }
      Blob createBlob = storage.createFrom(blobInfo, Paths.get(file.getAbsolutePath()), precondition);
      GCPCloudStoragePath storagePath = new GCPCloudStoragePath(createBlob.getSelfLink(), createBlob.getMediaLink());
      return storagePath;
    } catch (Exception e) {
      throw new RuntimeException("Error uploading file to GCP : " + e.getMessage());
    }
  }

  public GCPCloudStoragePath uploadFile(String filepath, String contents, String bucketName) throws RuntimeException {
    try {
      BlobId blobId = BlobId.of(bucketName, filepath);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      byte[] content = contents.getBytes(StandardCharsets.UTF_8);
      Storage.BlobTargetOption precondition;
      if (storage.get(bucketName, filepath) == null) {
        precondition = Storage.BlobTargetOption.doesNotExist();
      } else {
        precondition = Storage.BlobTargetOption.generationMatch(
            storage.get(bucketName, filepath).getGeneration());
      }
      Blob createBlob = storage.create(blobInfo, content, precondition);
      GCPCloudStoragePath storagePath = new GCPCloudStoragePath(createBlob.getSelfLink(), createBlob.getMediaLink());
      return storagePath;
    } catch (Exception e) {
      throw new RuntimeException("Error uploading file to GCP : " + e.getMessage());
    }
  }
}
