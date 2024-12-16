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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgStorageTest;
import com.google.cloud.storage.Notification;
import com.google.cloud.storage.NotificationInfo.EventType;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;

import java.io.IOException;

@SpringBootTest(classes = CfgStorageTest.class)
@TestPropertySource(properties = {
        "spring.application.name=service-testutil",
        "spring.application.scope=GLOBAL",
        "spring.domain=localhost",
        "meter.registry.stackdriver.enabled=false", // Disable Stackdriver
        "meter.registry.logging.enabled=true"
})
public class GcpCloudStorageServiceTest extends AbstractCommonTest {

    @Autowired
    private Storage storage;

    @Autowired
    private GcpCloudStorageService gcpCloudStorageService;

    @Test
    public void testGetBucketNotExisting() {
        assertFalse(gcpCloudStorageService.hasBucket("test-bucket-not-existing"));
    }

    @Test
    public void testCreateBucketNullName() {
        try {
            gcpCloudStorageService.createBucketIfNeeded(null, "us-central1");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Bucket name cannot be null"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCreateBucket() {
        try {
            gcpCloudStorageService.createBucketIfNeeded("test-bucket-create", "us-central1");
        } catch (StorageException e) {
            fail();
        }
        assertTrue(gcpCloudStorageService.hasBucket("test-bucket-create"));
    }

    @Test
    public void testCreateBucketAndAssignTopic() {
        Notification notification = Mockito.mock(Notification.class);
        doReturn(notification).when(storage).createNotification(any(), any());

        try {
            gcpCloudStorageService.createBucketIfNeeded("test-bucket-assign", "us-central1");
        } catch (StorageException e) {
            fail();
        }
        assertTrue(gcpCloudStorageService.hasBucket("test-bucket-assign"));
        EventType[] eventTypes = { EventType.OBJECT_FINALIZE };
        try {
            gcpCloudStorageService.assignTopicToBucket("test-bucket", "test-topic", eventTypes, null);

        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testDestroyBucket() {
        try {
            gcpCloudStorageService.createBucketIfNeeded("test-bucket-destroy", "us-central1");
        } catch (StorageException e) {
            fail();
        }
        assertTrue(gcpCloudStorageService.hasBucket("test-bucket-destroy"));
        gcpCloudStorageService.destroyBucket("test-bucket-destroy");
        assertFalse(gcpCloudStorageService.hasBucket("test-bucket-destroy"));
    }

    @Test
    public void testUploadFile() {
        gcpCloudStorageService.createBucketIfNeeded("test-bucket", "us-central1");
        try {
            gcpCloudStorageService.uploadFile("/uploaded/upload.txt",
                    "test-bucket", "test-bucket");
            String test = gcpCloudStorageService.readFile("test-bucket", "/uploaded/upload.txt");
            assertTrue(test.equals("test-bucket"));
            gcpCloudStorageService.uploadFile("/uploaded/upload.txt", 
                    "test-bucket", "test-bucket");
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }

}
