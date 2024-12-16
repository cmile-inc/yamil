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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nishant-pentapalli
 */
@Configuration
public class CfgWireMockServerTest {

  private static final Logger logger = LoggerFactory.getLogger(CfgWireMockServerTest.class);

  @Value("${test.wiremock.port:8080}")
  private int port;

  @Bean
  public WireMockServer wireMockServer() {
    logger.info("Starting WireMock server on port {}", port);
    WireMockServer wireMockServer =
        new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));

    return wireMockServer;
  }
}
