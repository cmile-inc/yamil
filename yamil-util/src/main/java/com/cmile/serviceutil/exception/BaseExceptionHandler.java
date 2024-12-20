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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public abstract class BaseExceptionHandler {

  protected ErrorResponse createErrorResponse(String code, String message, String actualError) {
    ErrorResponse error = new ErrorResponse();
    ErrorResponse.Error errorInfo = new ErrorResponse.Error();
    errorInfo.setCode(code);
    errorInfo.setMessage(message);
    errorInfo.setActualError(actualError);
    error.errorInfo(errorInfo);
    return error;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public @ResponseBody ErrorResponse handleGenericException(
      final Exception exception, HttpServletRequest request) {
    return createErrorResponse(
        ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getCode(),
        ErrorResponseMessageEnum.ERROR_PROCESSING_REQUEST.getMessage(),
        exception.getMessage());
  }
}
