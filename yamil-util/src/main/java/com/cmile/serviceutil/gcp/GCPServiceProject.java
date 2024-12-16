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

package com.cmile.serviceutil.gcp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.util.Collections;

/**
 * @author nishant-pentapalli
 */
@Component
public class GCPServiceProject {

  private static final Logger logger = LoggerFactory.getLogger(GCPServiceProject.class);

  @Value(
      "${GOOGLE_APPLICATION_CREDENTIALS:${gcp.service.account.key.file.path:/var/certs/google/credentials.json}}")
  private String serviceAccountKeyFilePath;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${spring.application.scope}")
  private String appDeploymentScope;

  @Value("${spring.du:#{null}}")
  private String du;

  @Value("${spring.domain:api.cmile.com}")
  private String domain;

  private String gcpProjectId;
  private GoogleCredentials googleCredentials;

  private final GcpProjectIdProvider gcpProjectIdProvider;
  private WebClient webClient;

  public GCPServiceProject(GcpProjectIdProvider gcpProjectIdProvider)
      throws IOException, SecurityException {
    this.gcpProjectIdProvider = gcpProjectIdProvider;
  }

  @PostConstruct
  public void init() throws IOException {

    if (StringUtils.isBlank(applicationName)) {
      throw new RuntimeException(
          "Please specify the Application name (spring.application.name environment variable) is expected");
    }

    if (StringUtils.isBlank(appDeploymentScope) || !isValidApplicationScope(appDeploymentScope)) {
      throw new RuntimeException(
          "Please specify the Application scope (spring.application.scope environment variable) is expected. Valid values are : GLOBAL/DU");
    }

    if (StringUtils.isBlank(domain)) {
      throw new RuntimeException(
          "Please specify the domain value (spring.domain environment variable) is expected");
    }

    if (isDuScope() && StringUtils.isBlank(du)) {
      throw new RuntimeException(
          "Please provide DU value (spring.du environment variable) is expected for space-services");
    }

    logger.debug(
        "Initializing GCP Service Project for the application {} in project: {}",
        applicationName,
        gcpProjectIdProvider.getProjectId());

    this.gcpProjectId = gcpProjectIdProvider.getProjectId();
    this.googleCredentials = readGoogleCredentials();
  }

  private GoogleCredentials readGoogleCredentials() throws IOException {
    // Create InputStream from the specified file path
    InputStream inputStream = getInputStream(serviceAccountKeyFilePath);

    if (inputStream == null) {
      throw new FileNotFoundException(
          "Service account key file not found at: " + serviceAccountKeyFilePath);
    }

    // Create GoogleCredentials using the InputStream
    try (inputStream) {
      return GoogleCredentials.fromStream(inputStream)
          .createScoped(
              Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
    }
  }

  private InputStream getInputStream(String filePath) throws IOException {
    // Check if the path is a file in the filesystem
    File file = new File(filePath);
    if (file.exists()) {
      logger.info("Loading service account key file from filesystem: {}", filePath);
      return new FileInputStream(file);
    } else {
      logger.info("Loading service account key file from classpath: {}", filePath);
      // Attempt to load from classpath
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
      if (inputStream == null) {
        return null; // Return null if the resource is not found
      }
      return inputStream;
    }
  }

  public String getProjectId() {
    return gcpProjectId;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getGoogleServiceAccountFileLocation() {
    return this.serviceAccountKeyFilePath;
  }

  public String getDu() {
    return this.du;
  }

  public String getDomain() {
    return this.domain;
  }

  public GoogleCredentials getGoogleCredentials() {
    return googleCredentials;
  }

  public String getAppDeploymentScope() {
    return appDeploymentScope;
  }

  private boolean isDuScope() {
    return ApplicationScopeEnum.DU.name().equalsIgnoreCase(appDeploymentScope);
  }

  private boolean isValidApplicationScope(String scope) {
    try {
      ApplicationScopeEnum.valueOf(scope.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
