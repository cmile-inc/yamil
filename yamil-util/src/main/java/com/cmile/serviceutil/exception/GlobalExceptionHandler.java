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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class GlobalExceptionHandler extends BaseExceptionHandler {

  @ExceptionHandler(IOException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public @ResponseBody ErrorResponse handleIOException(
      final IOException exception, HttpServletRequest request) {
    return createErrorResponse(
        ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getCode(),
        ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getMessage(),
        exception.getMessage());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public @ResponseBody ErrorResponse handleEntityNotFoundException(
      final EntityNotFoundException exception) {
    return createErrorResponse(
        ErrorResponseMessageEnum.ENTITY_NOT_FOUND.getCode(),
        ErrorResponseMessageEnum.ENTITY_NOT_FOUND.getMessage(),
        exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody ErrorResponse handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException exception) {
    List<String> errors = new ArrayList<>();
    exception
        .getBindingResult()
        .getAllErrors()
        .forEach(
            (details) -> {
              String fieldName = ((FieldError) details).getField();
              String errorMessage = details.getDefaultMessage();
              errors.add(fieldName + " : " + errorMessage);
            });
    return createErrorResponse(
        ErrorResponseMessageEnum.REQUEST_VALUES_CHECK.getCode(),
        ErrorResponseMessageEnum.REQUEST_VALUES_CHECK.getMessage(),
        String.join(", ", errors));
  }
}
