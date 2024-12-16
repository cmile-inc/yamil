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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ErrorResponseTest {

  @Test
  void testErrorResponseCreation() {
    // Create an Error instance
    ErrorResponse.Error error = new ErrorResponse.Error();
    error.setActualError("Detailed error information");
    error.setCode("ERR001");
    error.setMessage("Error message");
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.errorInfo(error);

    // Assert the error details
    assertEquals("Error message", errorResponse.getError().getMessage());
    assertEquals("ERR001", errorResponse.getError().getCode());
    assertEquals("Detailed error information", errorResponse.getError().getActualError());
  }

  @Test
  void testErrorResponseWithoutError() {
    // Create an ErrorResponse instance without an error
    ErrorResponse errorResponse = new ErrorResponse();

    // Assert that the error field is null
    assertNull(errorResponse.getError());
  }
}
