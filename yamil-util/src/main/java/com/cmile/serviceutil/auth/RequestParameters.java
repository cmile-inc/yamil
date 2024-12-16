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
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is used to store the request parameters.
 *
 * @author nishant-pentapalli
 */
public class RequestParameters {

  public static final ParameterScope COMMON_SCOPE = () -> "CommonScope";

  enum PARAMS {
    PROJECT_ID(COMMON_SCOPE),
    REGION_ID(COMMON_SCOPE),
    ZONE_ID(COMMON_SCOPE),
    SERVICE_ID(ServiceScope.GLOBAL),
    DU_ID(ServiceScope.DU),
    SPACE_ID(ServiceScope.DU),
    CORRELATION_ID(COMMON_SCOPE),
    USER_ID(ClientScope.USER),
    CLIENT_ID(ClientScope.SPACE),
    CALLER_SERVICE_ID(ClientScope.SERVICE),
    REQUEST_ID(COMMON_SCOPE);

    private final ParameterScope parameterScope;

    PARAMS(ParameterScope parameterScope) {
      this.parameterScope = parameterScope;
    }

    PARAMS[] getParameters(ParameterScope parameterScope) {
      return Arrays.stream(PARAMS.values())
          .filter(param -> param.parameterScope.equals(parameterScope))
          .toArray(PARAMS[]::new);
    }
  }

  private final Map<String, Object> parameters;

  private RequestParameters(Map<String, Object> parameters) {
    this.parameters = ImmutableMap.copyOf(parameters);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String namespace, String key) {
    return (T) parameters.get(namespace + "." + key);
  }

  public Map<String, Object> get(Predicate<Map.Entry> predicate) {
    return parameters.entrySet().stream()
        .filter(predicate)
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static Builder builder(ParameterScope parameterScope) {
    return new Builder(parameterScope);
  }

  public static class Builder {
    private final String namespace;
    private final Map<String, Object> parameters = new HashMap<>();

    private Builder(ParameterScope parameterScope) {
      this.namespace = parameterScope.getScope();
    }

    public Builder add(String key, Object value) {
      parameters.put(namespace + "." + key, value);
      return this;
    }

    public RequestParameters build() {

      return new RequestParameters(parameters);
    }
  }
}
