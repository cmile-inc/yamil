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

package com.cmile.serviceutil.pubsub;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.job.PubSub.PubSubPublisher;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.serviceutil.pubsub.testsetup.CfgTestPubSubStimulate;
import com.cmile.serviceutil.pubsub.testsetup.FrMessageReceiver;
import com.cmile.serviceutil.util.ServiceUtilConstants;
import com.cmile.testutil.CfgMetricRegistryTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import com.cmile.testutil.pubsub.PubSubServiceTest;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.util.UUID;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {CfgTestPubSubStimulate.class, CfgMetricRegistryTest.class, FrMessageReceiver.class})
@EnableAutoConfiguration(exclude = {TaskSchedulingAutoConfiguration.class, LiquibaseAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableIntegration
@TestPropertySource(
    properties = {
        "spring.cloud.gcp.pubsub.executor.max-inbound-threads=100", // Adjust the number of threads
        "spring.cloud.gcp.pubsub.executor.max-outbound-threads=100", // Adjust the number of threads
        "spring.integration.poller.max-messages-per-poll=5",
        "spring.integration.poller.fixed-delay=1000"
    }
)
public class FrMessageReceiverIntegrationTest extends SpaceAbstractCommonTest {

    @Autowired
    @Qualifier(value = "testActivator")
    private MessageChannel frIngestionInboundChannel;  // DirectChannel bean for inbound messages

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private PubSubPublisher pubSubPublisher;

    @Autowired
    private PubSubServiceTest pubSubServiceTest;

    @Autowired
    private FrMessageReceiver frMessageReceiver;

    @BeforeEach
    public void setUp() {
        assertNotNull(frIngestionInboundChannel, "frIngestionInboundChannel should be available");
        assertNotNull(pubSubTemplate, "PubSubTemplate should be available");
    }

    @Test
    public void testSendMessageToInboundChannel() throws IOException, InterruptedException {

        String topic = "test-topic";
        String payload = "{\"sampleField\":\"sampleValue\"}";
        PubsubMessage.Builder pubsubMessageBuilder = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(payload))
                .putAttributes(MetricsService.SPACE_ID_TAG, RequestContext.getRequestContextDetails().getSpaceId())
                .putAttributes( ServiceUtilConstants.CORRELATION_ID, UUID.randomUUID().toString());

        pubsubMessageBuilder.build();

        PubsubMessage pubsubMessage = pubsubMessageBuilder.build();
        pubSubTemplate.publish(topic, pubsubMessage);

        Thread.sleep(4000);
        System.out.println("test complete");
    }
}
