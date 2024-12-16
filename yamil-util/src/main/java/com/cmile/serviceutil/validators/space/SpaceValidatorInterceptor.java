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

package com.cmile.serviceutil.validators.space;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class SpaceValidatorInterceptor implements HandlerInterceptor, Ordered {

  private final SpaceCacheManager spaceCacheManager;
  private final GCPServiceProject gcpServiceProject;

  public SpaceValidatorInterceptor(
      SpaceCacheManager spaceCacheManager, GCPServiceProject gcpServiceProject) {
    this.spaceCacheManager = spaceCacheManager;
    this.gcpServiceProject = gcpServiceProject;
  }

  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
      throws IOException {

    log.debug(
        "SpaceValidatorInterceptor: preHandle: Validating spaceId for request: {}",
        req.getRequestURI());
    if (ApplicationScopeEnum.GLOBAL.name().equalsIgnoreCase(gcpServiceProject.getAppDeploymentScope())) {
      return true;
    }
    if (res.isCommitted()) {
      log.debug(
          "SpaceValidatorInterceptor: preHandle: response is already committed for request: {}",
          req.getRequestURI());
      return false;
    } else if (res.getStatus() == HttpServletResponse.SC_FORBIDDEN
        || res.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return false;
    }
    log.debug(
        "SpaceValidatorInterceptor: preHandle: proceeding to extract spaceId for request: {}",
        req.getRequestURI());
    ContextInfo client = RequestContext.getRequestContextDetails();
    if (client == null) {
      throw new RuntimeException(
          "Space in requestContext not found, please set the configs properly");
    }
    String spaceId = client.getSpaceId();
    Object spaceDetails = spaceCacheManager.getCache(spaceId);
    Optional.ofNullable(spaceDetails)
        .orElseThrow(() -> new RuntimeException(String.format("Invalid spaceId: %s", spaceId)));

    log.debug("Finished space validation for request: {}", req.getRequestURI());

    return true;
  }

  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    log.debug(
        "Space Validation: Post handle (do nothing) for request: {}", request.getRequestURI());
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
