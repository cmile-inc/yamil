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

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.storage.GCPCloudStoragePath;
import com.cmile.serviceutil.storage.GcpCloudStorageService;
import com.cmile.serviceutil.storage.StoragePathEnum;
import com.cmile.testutil.CfgStorageTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import com.google.auth.ServiceAccountSigner;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CfgStorageTest.class})
public class SpaceGcpCloudStorageServiceTest extends SpaceAbstractCommonTest {
    private static final Logger logger = LoggerFactory.getLogger(SpaceGcpCloudStorageServiceTest.class);

    @Autowired
    GcpCloudStorageService gcpCloudStorageService;

    @Autowired
    Storage mockStorage;

    // Moved strings to static final variables
    private static final String BUCKET_NAME = "test-bucket";
    private static final String TEST_FILE_PATH = "test-file.txt";
    private static final String TEST_FILE_CONTENT = "Test content";
    private static final String NEW_BUCKET_NAME = "new-test-bucket";
    private static final String TEST_SIGNED_FILE_PATH = "test-signed-file.txt";
    private static final String CONTENT_TYPE = "text/plain";
    private static final String TEMP_FILE_PREFIX = "src/main/resources/test-credentials";
    private static final String TEMP_FILE_SUFFIX = ".json";
    private static final String ORIGINAL_FILE_PATH = StoragePathEnum.CONTAINER_UPLOAD.getPath() + "/test-move-file.txt";
    private static final String NEW_FILE_PATH = StoragePathEnum.CONTAINER_INPROGRESS.getPath() + "/test-move-file.txt";
    private static final String TEST_READ_FILE_PATH = "test-read-file.txt";

    @BeforeEach
    public void setup() throws IOException {
        // Ensure bucket is created for each test case
        gcpCloudStorageService.createBucketIfNeeded(GcpSpacesNamingStrategyUtils
                .bucketName(RequestContext.getRequestContextDetails().getSpaceId()), "us-east4");
    }

    @AfterEach
    public void tearDown() {
        // Destroy buckets used in tests
        gcpCloudStorageService.destroyBucket(BUCKET_NAME);
        gcpCloudStorageService.destroyBucket(GcpSpacesNamingStrategyUtils
                .bucketName(RequestContext.getRequestContextDetails().getSpaceId()));
    }

    @Test
    void testHasBucket_notExist() {
        gcpCloudStorageService.destroyBucket(BUCKET_NAME);
        boolean exists = gcpCloudStorageService.hasBucket("nonexistent-bucket");
        assertFalse(exists);
    }

    @Test
    void testHasBucket_exist() {
        gcpCloudStorageService.createBucketIfNeeded(BUCKET_NAME, "us-east4");
        boolean exists = gcpCloudStorageService.hasBucket(BUCKET_NAME);
        assertTrue(exists);
    }

    @Test
    void testCreateBucketIfNeeded() {
        gcpCloudStorageService.createBucketIfNeeded(NEW_BUCKET_NAME, "us-east4");
        assertTrue(gcpCloudStorageService.hasBucket(NEW_BUCKET_NAME));
    }

    @Test
    void testDestroyBucket() {
        gcpCloudStorageService.destroyBucket(BUCKET_NAME);
        assertFalse(gcpCloudStorageService.hasBucket(BUCKET_NAME));
    }

    //    @Test
    void testUploadFile_File() throws IOException {
        File dummyFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);

        // Write some dummy content to the temporary file
        try (FileWriter writer = new FileWriter(dummyFile)) {
            String dummyContent = "{ \"key\": \"dummyValue\" }"; // Example JSON content
            writer.write(dummyContent);
        }

        // Ensure the file exists before proceeding (this should always be true for a temp file)
        if (!dummyFile.exists()) {
            throw new IOException("File not found: " + dummyFile.getAbsolutePath());
        }

        String bucket = GcpSpacesNamingStrategyUtils.bucketName(RequestContext.getRequestContextDetails().getSpaceId());
        gcpCloudStorageService.createBucketIfNeeded(bucket, "us-east4");
        GCPCloudStoragePath path = gcpCloudStorageService.uploadFile(dummyFile);
        assertNotNull(path);
        assertTrue(gcpCloudStorageService.hasBucket(BUCKET_NAME));
    }

    @Test
    void testUploadFile_String() throws IOException {
        String bucket = GcpSpacesNamingStrategyUtils.bucketName(RequestContext.getRequestContextDetails().getSpaceId());
        gcpCloudStorageService.createBucketIfNeeded(bucket, "us-east4");
        GCPCloudStoragePath path = gcpCloudStorageService.uploadFile(TEST_FILE_PATH, TEST_FILE_CONTENT);
        assertNotNull(path);
    }

    //    @Test
    void testGetSignedUrl() {
        String filePath = TEST_SIGNED_FILE_PATH;
        URL mockUrl = Mockito.mock(URL.class);
        ServiceAccountSigner serviceAccountSigner = Mockito.mock(ServiceAccountSigner.class);
        long expirationTime = 15;

        BlobId blobId = BlobId.of(BUCKET_NAME, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        gcpCloudStorageService.createBucketIfNeeded(BUCKET_NAME, "us-east4");

        when(mockStorage.signUrl(
                eq(blobInfo),
                eq(expirationTime),
                eq(TimeUnit.MINUTES),
                any())
        ).thenReturn(mockUrl);

        URL signedUrl = gcpCloudStorageService.getSignedUrl(BUCKET_NAME, StoragePathEnum.CONTAINER_UPLOAD,  filePath, CONTENT_TYPE);

        assertNotNull(signedUrl);
        assertEquals(mockUrl, signedUrl);
    }

    @Test
    void testMoveFile() throws IOException {
        String bucket = GcpSpacesNamingStrategyUtils.bucketName(RequestContext.getRequestContextDetails().getSpaceId());
        gcpCloudStorageService.createBucketIfNeeded(bucket, "us-east4");
        gcpCloudStorageService.uploadFile(ORIGINAL_FILE_PATH, TEST_FILE_CONTENT);
        String newObjectId = gcpCloudStorageService.moveFile(bucket, ORIGINAL_FILE_PATH,  StoragePathEnum.CONTAINER_UPLOAD, StoragePathEnum.CONTAINER_INPROGRESS);
        assertEquals(NEW_FILE_PATH, newObjectId);
    }

    @Test
    void testReadFile() throws IOException {
        String bucket = GcpSpacesNamingStrategyUtils.bucketName(RequestContext.getRequestContextDetails().getSpaceId());
        gcpCloudStorageService.createBucketIfNeeded(bucket, "us-east4");

        gcpCloudStorageService.uploadFile(TEST_READ_FILE_PATH, TEST_FILE_CONTENT);
        String fileContents = gcpCloudStorageService.readFile(bucket, TEST_READ_FILE_PATH);
        assertEquals(TEST_FILE_CONTENT, fileContents);
    }
}
