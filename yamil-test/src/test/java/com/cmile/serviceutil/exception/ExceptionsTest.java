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

package com.cmile.serviceutil.exception;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionsTest {

    @Test
    void testConnectionParamsNotFoundExceptionStatus() {
        // Arrange: Set up a MockHttpServletRequest and MockHttpServletResponse
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create a ResponseStatusExceptionResolver to handle the exception
        HandlerExceptionResolver resolver = new ResponseStatusExceptionResolver();

        // Act: Resolve the exception as Spring would in a controller
        resolver.resolveException(
                request, response, null, new ConnectionParamsNotFoundException("Test message")
        );

        // Assert: Verify the status
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    void testGCPFileUploadExceptionStatus() {
        // Arrange: Set up a MockHttpServletRequest and MockHttpServletResponse
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create a ResponseStatusExceptionResolver to handle the exception
        HandlerExceptionResolver resolver = new ResponseStatusExceptionResolver();

        // Act: Resolve the exception as Spring would in a controller
        resolver.resolveException(
                request, response, null, new GCPFileUploadException("Test message")
        );

        // Assert: Verify the status
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
    }

}
