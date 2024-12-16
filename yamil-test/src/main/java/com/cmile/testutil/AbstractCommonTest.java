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

package com.cmile.testutil;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.common.json.JsonEntityMapper;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.testutil.lisener.CustomApplicationContextInitializer;
import com.cmile.testutil.lisener.CustomTestContextListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.*;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Abstract class for common test setup and teardown.
 *
 * @author nishant-pentapalli
 */
@Component
@TestPropertySource(
    properties = {
      "spring.application.name=service-testutil",
      "spring.application.scope=GLOBAL",
      "spring.domain=http://localhost:9876",
      "gcp.service.account.key.file.path=testutil/test-credentials.json",
      "gcp.service.project=gcp-testutil",
      "meter.registry.stackdriver.enabled=false", // Disable Stackdriver
      "meter.registry.logging.enabled=true",
      "test.wiremock.port=9876",
      "mongodb.atlas.base.url=http://localhost:9876/api/atlas/v2/groups/"
    })
@ContextConfiguration(initializers = CustomApplicationContextInitializer.class)
@Import({CfgMetricRegistryTest.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestExecutionListeners(
    value = {CustomTestContextListener.class, DependencyInjectionTestExecutionListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class AbstractCommonTest {
  private static final Logger logger = LoggerFactory.getLogger(AbstractCommonTest.class);

  @Autowired protected LoggingMeterRegistry loggingMeterRegistry;
  @Autowired protected ObjectMapper objectMapper;
  @Autowired protected JsonEntityMapper jsonEntityMapper;
  @Autowired protected GCPServiceProject gcpServiceProject;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("GOOGLE_APPLICATION_CREDENTIALS", () -> "testutil/test-credentials.json");
  }

  @BeforeEach
  public void setUp() {
    logger.debug("Setting up the test");
  }

  @AfterEach
  public void tearDown() {
    logger.debug("Tearing down the test");
    RequestContext.clear();
  }

  public InputStream readFile(String file) {
    return getClass().getClassLoader().getResourceAsStream(file);
  }

  public String readJsonFromFile(String file) throws Exception {
    String jsonString = null;

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file)) {
      if (inputStream == null) {
        throw new RuntimeException("File not found: " + file);
      }

      // Read the JSON content from the input stream into a JsonNode
      JsonNode jsonNode = objectMapper.readTree(inputStream);
      // Convert JsonNode to a JSON string
      jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

    } catch (Exception e) {
      logger.error("Error while reading JSON from file", e);
      throw e;
    }

    return jsonString; // Return the JSON string
  }
}
