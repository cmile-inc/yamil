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

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.util.ServiceUtilConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Configuration
public class CfgLogging {
  public static final String SPACE_ID = "spaceId";
  public static final String DU_ID = "duId";
  public static final String USER_ID = "userId";
  public static final String REQUESTED_URL = "requestedUrl";

  @Bean
  public FilterRegistrationBean<LogFilter> logFilter() {

    Map<String, Function<HttpServletRequest, String>> mdcParams =
        Map.of(
            USER_ID,
            request -> extractUserId(),
                REQUESTED_URL,
            request -> extractUrl(request),
                ServiceUtilConstants.CORRELATION_ID,
            request -> extractCorrelationId(request),
            SPACE_ID,
            request -> extractSpaceId(),
            DU_ID,
            request -> extractDuId());
    FilterRegistrationBean<LogFilter> registrationBean = new FilterRegistrationBean<>();
    LogFilter logFilter = new LogFilter(mdcParams);
    registrationBean.setFilter(logFilter);
    registrationBean.addUrlPatterns("/*"); // Apply filter to all URLs
    registrationBean.setOrder(4);
    return registrationBean;
  }

  private String extractCorrelationId(HttpServletRequest request) {
    return RequestContext.getRequestContextDetails().getCorrelationId();
  }

  private String extractUrl(HttpServletRequest request) {
    return Optional.ofNullable(request.getRequestURL())
        .map(StringBuffer::toString)
        .orElse("Jobs/PubSub");
  }

  private String extractUserId() {
    String userId = "global";
    if (RequestContext.getRequestContextDetails() == null
        || RequestContext.getRequestContextDetails().getAdditionalInfo() == null
        || RequestContext.getRequestContextDetails().getAdditionalInfo().isEmpty()) {
      return userId;
    }
    return RequestContext.getRequestContextDetails().getAdditionalInfo().get("cmileUserId");
  }

  private String extractDuId() {
    String duId = "global";
    if (RequestContext.getRequestContextDetails() == null
        || RequestContext.getRequestContextDetails().getDuId() == null) {
      return duId;
    }
    return RequestContext.getRequestContextDetails().getDuId();
  }

  private String extractSpaceId() {
    String duId = "global";
    if (RequestContext.getRequestContextDetails() == null
        || RequestContext.getRequestContextDetails().getSpaceId() == null) {
      return duId;
    }
    return RequestContext.getRequestContextDetails().getSpaceId();
  }
}
