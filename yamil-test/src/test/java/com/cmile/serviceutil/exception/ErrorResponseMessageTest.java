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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorResponseMessageTest {

    @Test
    void testErrorResponseMessageEnumValues() {
        // Test ERROR_PROCESSING_REQUEST values
        assertEquals("ERROR_PROCESSING_REQUEST", ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getCode());
        assertEquals("Error Processing request. Please try again!", ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getMessage());

        // Test ENTITY_NOT_FOUND values
        assertEquals("ENTITY_NOT_FOUND", ErrorResponseMessageEnum.ENTITY_NOT_FOUND.getCode());
        assertEquals("Entity not found, Please provide correct id!", ErrorResponseMessageEnum.ENTITY_NOT_FOUND.getMessage());

        // Test REQUEST_VALUES_CHECK values
        assertEquals("REQUEST_VALUES_CHECK", ErrorResponseMessageEnum.REQUEST_VALUES_CHECK.getCode());
        assertEquals("Please check the request payload/value", ErrorResponseMessageEnum.REQUEST_VALUES_CHECK.getMessage());
    }

    @Test
    void testErrorResponseInfo() {
        ErrorResponse errorResponse = new ErrorResponse();
        ErrorResponse.Error errorInfo = new ErrorResponse.Error();

        // Set values for the error object
        errorInfo.setCode("TEST_CODE");
        errorInfo.setMessage("Test message");
        errorInfo.setActualError("Detailed error message");

        // Set error information in ErrorResponse
        errorResponse.errorInfo(errorInfo);

        // Validate that the error was set correctly
        assertEquals("TEST_CODE", errorResponse.getError().getCode());
        assertEquals("Test message", errorResponse.getError().getMessage());
        assertEquals("Detailed error message", errorResponse.getError().getActualError());
    }

    @Test
    void testErrorResponseWithNullValues() {
        ErrorResponse errorResponse = new ErrorResponse();
        ErrorResponse.Error errorInfo = new ErrorResponse.Error();

        // Only set one field to check JSON non-null behavior
        errorInfo.setCode("TEST_CODE");

        errorResponse.errorInfo(errorInfo);

        // Validate fields
        assertEquals("TEST_CODE", errorResponse.getError().getCode());
        assertNull(errorResponse.getError().getMessage());
        assertNull(errorResponse.getError().getActualError());
    }
}
