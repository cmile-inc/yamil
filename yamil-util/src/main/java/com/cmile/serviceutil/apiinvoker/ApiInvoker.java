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

package com.cmile.serviceutil.apiinvoker;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ClientScope;
import com.cmile.serviceutil.auth.jwt.JwtTokenProvider;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

@Component
@Slf4j
public class ApiInvoker {

  // Constants for string literals
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ERROR_LOADING_API_DETAILS =
      "An error occurred while interacting with the API client. Error: %s";
  private static final String ERROR_INTERACTING_API =
      "An error occurred while interacting with the API client. Error: %s";
  private static final String SET_BASE_PATH_METHOD = "setBasePath";
  private static final String ADD_DEFAULT_HEADER_METHOD = "addDefaultHeader";
  private static final String KEY_CID = "cid";
  private static final String KEY_TID = "tid";

  private final GCPServiceProject gcpServiceProject;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public ApiInvoker(GCPServiceProject gcpServiceProject, JwtTokenProvider jwtTokenProvider) {
    this.gcpServiceProject = gcpServiceProject;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public <T> T invoke(Object apiClientInstance, Callable<Mono<T>> apiMethodCallable) {
    try {
      String token =
          jwtTokenProvider.createToken(
              ClientScope.SERVICE,
              gcpServiceProject.getApplicationName(),
              Optional.ofNullable(RequestContext.getRequestContextDetails())
                  .map(
                      details ->
                          Optional.ofNullable(details.getAdditionalInfo())
                              .map(props -> props.get(KEY_CID))
                              .orElse(null))
                  .orElse(null),
              Optional.ofNullable(RequestContext.getRequestContextDetails())
                  .map(val -> val.getSpaceId())
                  .orElse(null),
              Optional.ofNullable(RequestContext.getRequestContextDetails())
                  .map(
                      details ->
                          Optional.ofNullable(details.getAdditionalInfo())
                              .map(props -> props.get(KEY_TID))
                              .orElse(null))
                  .orElse(null),
              gcpServiceProject.getDu());

      if (apiClientInstance != null) {
        setBasePath(apiClientInstance, gcpServiceProject.getDomain());
        addAuthorizationHeader(apiClientInstance, token);
      }

      Mono<T> resultMono = apiMethodCallable.call();

      return resultMono
          .doOnNext(
              result -> {
                if (result == null) {
                  log.debug("API result is null");
                }
              })
          .doOnError(
              error -> {
                throw new RuntimeException(
                    String.format(ERROR_LOADING_API_DETAILS, error.getMessage()), error);
              })
          .block();

    } catch (Exception e) {
      throw new RuntimeException(String.format(ERROR_INTERACTING_API, e.getMessage()), e);
    }
  }

  private void setBasePath(Object apiClientInstance, String basePath) throws Exception {
    Method setBasePathMethod =
        apiClientInstance.getClass().getMethod(SET_BASE_PATH_METHOD, String.class);
    setBasePathMethod.invoke(apiClientInstance, basePath);
  }

  private void addAuthorizationHeader(Object apiClientInstance, String token) throws Exception {
    Method addDefaultHeaderMethod =
        apiClientInstance
            .getClass()
            .getMethod(ADD_DEFAULT_HEADER_METHOD, String.class, String.class);
    addDefaultHeaderMethod.invoke(apiClientInstance, AUTHORIZATION_HEADER, BEARER_PREFIX + token);
  }
}
