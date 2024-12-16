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

package com.cmile.serviceutil.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;

/**
 * Extracts service type specific parameters from the request.
 *
 * @author nishant-pentapalli
 */
public enum ServiceScope implements ParameterScope, ParameterExtractor {
  GLOBAL(
      (request, claims, environment, scope) ->
          RequestParameters.builder(scope).add("", "").build()),
  DU(
      (request, claims, environment, scope) ->
          RequestParameters.builder(scope).add("duId", "duId").add("tenantId", "tenantId").build());

  private final String namespace;
  private final ParameterExtractor extractor;

  ServiceScope(ParameterExtractor extractor) {
    this.namespace = this.name().toLowerCase();
    this.extractor = extractor;
  }

  @Override
  public String getScope() {
    return this.namespace;
  }

  @Override
  public RequestParameters extract(
      HttpServletRequest request,
      Claims claims,
      Environment environment,
      ParameterScope namespace) {
    return extractor.extract(request, claims, environment, this);
  }
}
