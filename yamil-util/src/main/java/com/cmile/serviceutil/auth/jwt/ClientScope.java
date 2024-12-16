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

package com.cmile.serviceutil.auth.jwt;

import com.cmile.serviceutil.auth.ParameterExtractor;
import com.cmile.serviceutil.auth.ParameterScope;
import com.cmile.serviceutil.auth.RequestParameters;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;

/**
 * The type of client that issued the token
 *
 * @author nishant-pentapalli
 */
public enum ClientScope implements ParameterScope, ParameterExtractor {
  USER("https://securetoken.google.com/") {
    @Override
    ContextInfo getRequestContext(Claims claims) {
      return ContextExtractor.getUserContext(claims);
    }
  },
  SPACE("https://api.cmile.com/token/") {
    @Override
    ContextInfo getRequestContext(Claims claims) {
      return ContextExtractor.getSpaceContext(claims);
    }
  },
  SERVICE("https://api.cmile.com/service/") {
    @Override
    ContextInfo getRequestContext(Claims claims) {
      return ContextExtractor.getServiceContext(claims);
    }
  };

  private final String namespace;
  private final String issuerPrefix;

  ClientScope(String issuerPrefix) {
    this.namespace = this.name().toLowerCase();
    this.issuerPrefix = issuerPrefix;
  }

  public static ClientScope fromIssuer(String issuer) {
    for (ClientScope clientType : values()) {
      if (issuer.startsWith(clientType.issuerPrefix)) {
        return clientType;
      }
    }
    throw new IllegalArgumentException("Unknown issuer: " + issuer);
  }

  public static ContextInfo fromClaims(Claims claims) {
    ClientScope clientType = fromIssuer(claims.getIssuer());
    return clientType.getRequestContext(claims);
  }

  abstract ContextInfo getRequestContext(Claims claims);

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
    return null;
  }
}
