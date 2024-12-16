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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseExceptionHandlerTest {

  private TestExceptionHandler testExceptionHandler;
  private HttpServletRequest mockRequest;

  @BeforeEach
  void setUp() {
    testExceptionHandler = new TestExceptionHandler();
    mockRequest = mock(HttpServletRequest.class);
  }

  @Test
  void testCreateErrorResponse() {
    String code = "ERR001";
    String message = "An error occurred";
    String actualError = "Detailed error information";

    ErrorResponse response =
        testExceptionHandler.createErrorResponse(code, message, actualError);

    assertEquals(code, response.getError().getCode());
    assertEquals(message, response.getError().getMessage());
    assertEquals(actualError, response.getError().getActualError());
  }

  @Test
  void testHandleGenericException() {
    Exception mockException = new RuntimeException("Test exception");

    ErrorResponse response =
        testExceptionHandler.handleGenericException(mockException, mockRequest);

    assertEquals(
        ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getCode(),
        response.getError().getCode());
    assertEquals(
        ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getMessage(),
        response.getError().getMessage());
    assertEquals(mockException.getMessage(), response.getError().getActualError());
  }
}
