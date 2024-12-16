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

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseExceptionHandlerTest {

    private BaseExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new BaseExceptionHandler() {
        };
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Test error message");

        // Act
        ErrorResponse response = exceptionHandler.handleGenericException(exception, request);

        // Assert
        assertEquals(ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getCode(),
                response.getError().getCode());
        assertEquals(ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getMessage(),
                response.getError().getMessage());
        assertEquals("Test error message",
                response.getError().getActualError());
    }
}
