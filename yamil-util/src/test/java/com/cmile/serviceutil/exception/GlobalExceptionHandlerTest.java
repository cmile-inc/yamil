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
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler globalExceptionHandler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
  }

  @Test
  void testHandleIOException() {
    IOException ioException = new IOException("File not found");

    ErrorResponse response = globalExceptionHandler.handleIOException(ioException, request);

    assertEquals("ERROR_PROCESSING_REQUEST", response.getError().getCode());
    assertEquals("Error Processing request. Please try again!", response.getError().getMessage());
    assertEquals("File not found", response.getError().getActualError());
  }

  @Test
  void testHandleEntityNotFoundException() {
    EntityNotFoundException entityNotFoundException =
        new EntityNotFoundException("Entity not found");

    ErrorResponse response =
        globalExceptionHandler.handleEntityNotFoundException(entityNotFoundException);

    assertEquals("ENTITY_NOT_FOUND", response.getError().getCode());
    assertEquals("Entity not found, Please provide correct id!", response.getError().getMessage());
    assertEquals("Entity not found", response.getError().getActualError());
  }

  @Test
  void testHandleMethodArgumentNotValidException() {
    // Create a mock BindingResult
    BindingResult bindingResult = mock(BindingResult.class);

    // Create a mock MethodArgumentNotValidException and set up the BindingResult
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);

    // Mock FieldError and BindingResult behavior
    FieldError fieldError = mock(FieldError.class);
    when(fieldError.getField()).thenReturn("fieldName");
    when(fieldError.getDefaultMessage()).thenReturn("errorMessage");

    // Setup the BindingResult to return a list with the mocked FieldError
    when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

    ErrorResponse response =
        globalExceptionHandler.handleMethodArgumentNotValidException(exception);

    assertEquals("REQUEST_VALUES_CHECK", response.getError().getCode());
    assertEquals("Please check the request payload/value", response.getError().getMessage());
    assertEquals("fieldName : errorMessage", response.getError().getActualError());
  }
}
