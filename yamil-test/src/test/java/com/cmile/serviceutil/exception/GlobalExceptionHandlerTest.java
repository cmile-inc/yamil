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

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void testHandleIOException() {
        IOException ioException = new IOException("IO error occurred");

        ErrorResponse response = exceptionHandler.handleIOException(ioException, mockRequest);

        assertEquals(ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getCode(), response.getError().getCode());
        assertEquals(ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getMessage(), response.getError().getMessage());
        assertEquals("IO error occurred", response.getError().getActualError());
    }

    @Test
    void testHandleEntityNotFoundException() {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException("Entity not found");

        ErrorResponse response = exceptionHandler.handleEntityNotFoundException(entityNotFoundException);

        assertEquals(ErrorResponseMessageEnum.ENTITY_NOT_FOUND.getCode(), response.getError().getCode());
        assertEquals(ErrorResponseMessageEnum.ENTITY_NOT_FOUND.getMessage(), response.getError().getMessage());
        assertEquals("Entity not found", response.getError().getActualError());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        BindException bindException = new BindException(new Object(), "target");

        // Simulate field validation errors
        bindException.addError(new FieldError("target", "field1", "must not be null"));
        bindException.addError(new FieldError("target", "field2", "must be greater than zero"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindException);

        ErrorResponse response = exceptionHandler.handleMethodArgumentNotValidException(exception);

        assertEquals(ErrorResponseMessageEnum.REQUEST_VALUES_CHECK.getCode(), response.getError().getCode());
        assertEquals(ErrorResponseMessageEnum.REQUEST_VALUES_CHECK.getMessage(), response.getError().getMessage());
        assertEquals("field1 : must not be null, field2 : must be greater than zero", response.getError().getActualError());
    }
}
