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

package com.cmile.serviceutil.spacevalidator;

import com.cmile.serviceutil.apiinvoker.ApiInvoker;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.validators.space.SpaceCacheManager;
import com.cmile.serviceutil.validators.space.SpaceDetails;
import com.cmile.serviceutil.validators.space.SpacePlatformService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpaceCacheManagerTest {

    @Mock
    private GCPServiceProject gcpServiceProject;

    @Mock
    private ApiInvoker apiInvoker;

    @Mock
    private SpacePlatformService platformService;

    private SpaceCacheManager spaceCacheManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        spaceCacheManager = new SpaceCacheManager(
            gcpServiceProject,
            apiInvoker,
            objectMapper,
            platformService
        );
    }

    @Test
    public void testLoadSpaceDetails_Success() {
        String spaceId = "test-space-id";
        SpaceDetails mockSpaceDetails = new SpaceDetails();

        when(platformService.getSpaceDetails(spaceId)).thenReturn(mockSpaceDetails);

        Object result = spaceCacheManager.getCache(spaceId);
        assertNotNull(result);
        assertEquals(mockSpaceDetails, result);

        verify(platformService, times(1)).getSpaceDetails(spaceId);
    }

    @Test
    public void testLoadSpaceDetails_Error() {
        String spaceId = "test-space-id";

        when(platformService.getSpaceDetails(spaceId))
            .thenThrow(new RuntimeException("Failed to fetch space details"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spaceCacheManager.getCache(spaceId);
        });

        assertEquals("Failed to fetch space details", exception.getMessage());
        verify(platformService, times(1)).getSpaceDetails(spaceId);
    }
}
