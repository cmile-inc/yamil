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

import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgAuthTest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author nishant-pentapalli
 */
@SpringBootTest(classes = {CfgAuthTest.class})
public class JwtProviderTest extends AbstractCommonTest {
  private static final Logger logger = LoggerFactory.getLogger(JwtProviderTest.class);

  @Autowired JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private GCPServiceProject gcpServiceProject; // Inject GCPServiceProject

  @Autowired
  private SecretManagerServiceClient
          secretManagerServiceClient; // Inject SecretManagerServiceClient

  @BeforeEach
  public void setUp() {
    logger.debug("Setting up the test");
  }

  @AfterEach
  public void tearDown() {
    logger.debug("Tearing down the test");
  }

  @Test
  public void testJwtProvider() {
    logger.debug("Testing JwtProvider");
    // Assertions.assertNotNull(jwtAuthenticationFilter);
  }
}
