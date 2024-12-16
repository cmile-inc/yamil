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

package com.cmile.serviceutil.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.MDC;

public class LogFilter implements Filter {

  private final Map<String, Function<HttpServletRequest, String>> mdcParams;

  public LogFilter(Map<String, Function<HttpServletRequest, String>> mdcParams) {
    this.mdcParams = mdcParams;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;

      // Iterate over the mdcParams map and put each entry into MDC
      mdcParams.forEach((key, extractor) -> MDC.put(key, extractor.apply(httpRequest)));

      // Continue with the filter chain
      chain.doFilter(request, response);
    } finally {
      // Ensure the MDC is cleared after the request is processed
      MDC.clear();
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization logic, if needed
  }

  @Override
  public void destroy() {
    // Cleanup logic, if needed
  }
}
