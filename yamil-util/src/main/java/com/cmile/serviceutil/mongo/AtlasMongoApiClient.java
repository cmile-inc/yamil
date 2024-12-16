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

package com.cmile.serviceutil.mongo;

import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import java.net.URI;
import java.text.ParseException;
import me.vzhilin.auth.DigestAuthenticator;
import me.vzhilin.auth.parser.ChallengeResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class AtlasMongoApiClient {
  private static final Logger logger = LoggerFactory.getLogger(AtlasMongoApiClient.class);

  // Static final variables for HTTP methods and content types
  private static final String APPLICATION_JSON_VND_ATLAS = "application/vnd.atlas.2023-01-01+json";
  private static final String HTTP_METHOD_DELETE = "DELETE";
  private static final String HTTP_METHOD_POST = "POST";
  private static final String HTTP_METHOD_GET = "GET";
  private static final String URL_FORMAT = "%s?&currentSchema=%s";

  private final WebClient webClient;
  private final DigestAuthenticator authenticator;

  public AtlasMongoApiClient(
      MongoAdminEntity mongoAdminEntity,
      WebClient.Builder webClientBuilder,
      @Value("${mongodb.atlas.base.url:https://cloud.mongodb.com/api/atlas/v2/groups}")
          String atlasBaseUrl) {

    logger.debug("Initializing AtlasMongoApiClient");

    this.webClient = webClientBuilder.baseUrl(atlasBaseUrl).build();
    this.authenticator =
        new DigestAuthenticator(mongoAdminEntity.getPublicKey(), mongoAdminEntity.getPrivateKey());
  }

  public Mono<String> deleteApi(String url) {
    if (authenticator == null) {
      return Mono.error(new RuntimeException("Authenticator is not initialized"));
    }

    URI uri = URI.create(url);
    RequestHeadersSpec<?> getRequest =
        webClient.delete().uri(uri).header(HttpHeaders.ACCEPT, APPLICATION_JSON_VND_ATLAS);
    return getRequest
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(
            WebClientResponseException.Unauthorized.class,
            error -> {
              String receivedAuthenticateHeader =
                  error.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
              try {
                authenticator.onResponseReceived(
                    new ChallengeResponseParser(receivedAuthenticateHeader).parseChallenge(),
                    error.getStatusCode().value());
              } catch (ParseException e) {
                return Mono.error(e);
              }
              String authorizationHeader =
                  authenticator.authorizationHeader(HTTP_METHOD_DELETE, url);
              return getRequest
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .retrieve()
                  .bodyToMono(String.class);
            });
  }

  public Mono<String> postApi(String url, String body) {
    if (authenticator == null) {
      return Mono.error(new RuntimeException("Authenticator is not initialized"));
    }

    URI uri = URI.create(url);
    RequestHeadersSpec<?> postRequest =
        webClient
            .post()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VND_ATLAS)
            .header(HttpHeaders.ACCEPT, APPLICATION_JSON_VND_ATLAS)
            .bodyValue(body);
    return postRequest
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(
            WebClientResponseException.Unauthorized.class,
            error -> {
              String receivedAuthenticateHeader =
                  error.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
              try {
                authenticator.onResponseReceived(
                    new ChallengeResponseParser(receivedAuthenticateHeader).parseChallenge(),
                    error.getStatusCode().value());
              } catch (ParseException e) {
                return Mono.error(e);
              }
              String authorizationHeader =
                  authenticator.authorizationHeader(
                      HTTP_METHOD_POST, "https://cloud.mongodb.com/api/atlas/v2/groups" + url);
              return postRequest
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .retrieve()
                  .bodyToMono(String.class);
            });
  }

  public Mono<String> getApi(String url) {
    if (authenticator == null) {
      return Mono.error(new RuntimeException("Authenticator is not initialized"));
    }

    URI uri = URI.create(url);
    RequestHeadersSpec<?> getRequest =
        webClient.get().uri(uri).header(HttpHeaders.ACCEPT, APPLICATION_JSON_VND_ATLAS);
    return getRequest
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(
            WebClientResponseException.Unauthorized.class,
            error -> {
              String receivedAuthenticateHeader =
                  error.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
              try {
                authenticator.onResponseReceived(
                    new ChallengeResponseParser(receivedAuthenticateHeader).parseChallenge(),
                    error.getStatusCode().value());
              } catch (ParseException e) {
                return Mono.error(e);
              }
              String authorizationHeader = authenticator.authorizationHeader(HTTP_METHOD_GET, url);
              return getRequest
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .retrieve()
                  .bodyToMono(String.class);
            });
  }
}
