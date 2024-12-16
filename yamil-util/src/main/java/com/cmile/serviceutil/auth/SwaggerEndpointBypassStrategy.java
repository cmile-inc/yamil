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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SwaggerEndpointBypassStrategy implements AuthorizationBypassStrategy {

  public SwaggerEndpointBypassStrategy() {}

  private static final List<String> SWAGGER_PATTERNS =
      Arrays.asList(
          "/swagger-ui/",
          "/v3/api-docs/",
          "/swagger-resources/",
          "/webjars/",
          "/favicon.ico",
          "/v3/api-docs");

  @Override
  public boolean shouldBypass(HttpServletRequest httpServletRequest) {
    String requestUri = httpServletRequest.getRequestURI();
    return SWAGGER_PATTERNS.stream().anyMatch(requestUri::contains);
  }
}
