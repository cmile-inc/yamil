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

public enum ErrorResponseMessageEnum {
  ERROR_PROCESSING_REQUEST(
      "ERROR_PROCESSING_REQUEST", "Error Processing request. Please try again!"),
  ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "Entity not found, Please provide correct id!"),
  REQUEST_VALUES_CHECK("REQUEST_VALUES_CHECK", "Please check the request payload/value");

  private final String code;
  private final String message;

  ErrorResponseMessageEnum(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }

  public String getCode() {
    return code;
  }
}
