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

import com.cmile.serviceutil.auth.jwt.ClientScope;
import com.google.common.base.Preconditions;

/**
 * @author nishant-pentapalli
 */
public class RequestContext2 {
  private final ServiceScope serviceScope;
  private final ClientScope requestScope;
  private final RequestParameters parameters;

  // Private constructor to enforce immutability
  private RequestContext2(Builder builder) {
    this.requestScope = builder.requestScope;
    this.serviceScope = builder.serviceScope;
    this.parameters = builder.parameters;
  }

  public static String getProjectId() {
    RequestContext2 currentContext = RequestContextHolder.getCurrentContext();
    return RequestParameters.PARAMS.PROJECT_ID.name();
  }

  public static String getRegionId() {
    return RequestParameters.PARAMS.REGION_ID.name();
  }

  public static String getZoneId() {
    return RequestParameters.PARAMS.ZONE_ID.name();
  }

  public static String getServiceId() {
    return RequestParameters.PARAMS.SERVICE_ID.name();
  }

  public static String getDuId() {
    return RequestParameters.PARAMS.DU_ID.name();
  }

  public static String getSpaceId() {
    return RequestParameters.PARAMS.SPACE_ID.name();
  }

  public static String getCorrelationId() {
    return RequestParameters.PARAMS.CORRELATION_ID.name();
  }

  public static String getUserId() {
    return RequestParameters.PARAMS.USER_ID.name();
  }

  public static String getClientId() {
    return RequestParameters.PARAMS.CLIENT_ID.name();
  }

  public static String getCallerServiceId() {
    return RequestParameters.PARAMS.CALLER_SERVICE_ID.name();
  }

  public static String getRequestId() {
    return RequestParameters.PARAMS.REQUEST_ID.name();
  }

  static Builder builder() {
    return new Builder();
  }

  // Builder pattern for constructing RequestContext
  public static class Builder {
    private ClientScope requestScope;
    private ServiceScope serviceScope;
    private RequestParameters parameters; // Unified parameters

    private Builder() {}

    public Builder withRequestScope(ClientScope requestScope) {
      this.requestScope = requestScope;
      return this;
    }

    public Builder withServiceScope(ServiceScope serviceScope) {
      this.serviceScope = serviceScope;
      return this;
    }

    public Builder withParameters(RequestParameters parameters) {
      this.parameters = parameters;
      return this;
    }

    public RequestContext2 build() {
      Preconditions.checkState(
          RequestContextHolder.getCurrentContext() == null, "Context already set");
      Preconditions.checkArgument(requestScope != null, "RequestScope is required");
      Preconditions.checkArgument(serviceScope != null, "ServiceScope is required");
      Preconditions.checkArgument(parameters != null, "Parameters are required");

      RequestContext2 requestContext = new RequestContext2(this);
      RequestContextHolder.setCurrentContext(requestContext);
      return requestContext;
    }
  }
}
