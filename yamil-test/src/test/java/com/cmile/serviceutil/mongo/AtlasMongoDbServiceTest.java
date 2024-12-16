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

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.cmile.serviceutil.secret.entity.MongoAdminEntity;
import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgMongoTest;
import com.cmile.testutil.CfgWireMockServerTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

@SpringBootTest(classes = {CfgWireMockServerTest.class, CfgMongoTest.class})
public class AtlasMongoDbServiceTest extends AbstractCommonTest {
  private AtlasMongoDbService atlasMongoDbService;
  private MongoAdminEntity mongoAdminEntity;

  @Autowired
  public AtlasMongoDbServiceTest(
      MongoAdminEntity mongoAdminEntity, AtlasMongoDbService atlasMongoDbService) {
    super();
    this.atlasMongoDbService = atlasMongoDbService;
    this.mongoAdminEntity = mongoAdminEntity;
  }

  @Autowired private WireMockServer wireMockServer;

  @BeforeEach
  public void setUp() {
    wireMockServer.start();
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
  }

  // @Test
  public void testMongoUserCreate() {
    mockAtlasServerReponse();
    // Assert initial state: User does not exist
    assertFalse(atlasMongoDbService.userExists("test-user"));

    // Create user
    atlasMongoDbService.createUserWithDatabaseAccess("cmile-test", "test-user", "test-password");

    // Verify user exists after creation
    assertTrue(atlasMongoDbService.userExists("test-user"));

    // Try to create user again -> expect exception due to conflict
    assertThrows(
        RuntimeException.class,
        () ->
            atlasMongoDbService.createUserWithDatabaseAccess(
                "cmile-test", "test-user", "test-password"));

    // Cleanup user
    atlasMongoDbService.deleteUser("test-user");

    // Verify user does not exist after deletion
    assertFalse(atlasMongoDbService.userExists("test-user"));
  }

  private void mockAtlasServerReponse() {
    String basePathUrl = "/api/atlas/v2/groups/" + mongoAdminEntity.getProjectId();

    // Initial state: User does not exist
    wireMockServer.stubFor(
        get(urlPathEqualTo(basePathUrl + "/databaseUsers/admin/test-user"))
            .inScenario("User Management")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse().withBody("{}").withStatus(HttpStatus.NOT_FOUND.value())));

    // User creation success
    wireMockServer.stubFor(
        post(urlPathEqualTo(basePathUrl + "/databaseUsers"))
            .inScenario("User Management")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse().withBody("{}").withStatus(HttpStatus.CREATED.value()))
            .willSetStateTo("User Created"));

    // After user created: User exists
    wireMockServer.stubFor(
        get(urlPathEqualTo(basePathUrl + "/databaseUsers/admin/test-user"))
            .inScenario("User Management")
            .whenScenarioStateIs("User Created")
            .willReturn(aResponse().withBody("{}").withStatus(HttpStatus.OK.value())));

    // Conflict on trying to create the same user again
    wireMockServer.stubFor(
        post(urlPathEqualTo(basePathUrl + "/databaseUsers"))
            .inScenario("User Management")
            .whenScenarioStateIs("User Created")
            .willReturn(aResponse().withStatus(HttpStatus.CONFLICT.value())));

    // Cleanup: Successful user deletion
    wireMockServer.stubFor(
        delete(urlPathEqualTo(basePathUrl + "/databaseUsers/admin/test-user"))
            .inScenario("User Management")
            .whenScenarioStateIs("User Created")
            .willReturn(aResponse().withBody("{}").withStatus(HttpStatus.OK.value()))
            .willSetStateTo("User Deleted"));

    // After deletion: User does not exist
    wireMockServer.stubFor(
        get(urlPathEqualTo(basePathUrl + "/databaseUsers/admin/test-user"))
            .inScenario("User Management")
            .whenScenarioStateIs("User Deleted")
            .willReturn(aResponse().withBody("{}").withStatus(HttpStatus.NOT_FOUND.value())));
  }
}
