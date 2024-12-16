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
import com.cmile.testutil.CfgPubSubTest;
import com.cmile.testutil.SpaceAbstractCommonTest;
import com.cmile.testutil.pubsub.PubSubServiceTest;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import com.google.pubsub.v1.Subscription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest(classes = {CfgPubSubTest.class})
@TestPropertySource(
        properties = {
                "spring.cloud.gcp.pubsub.executor.max-inbound-threads=100", // Adjust the number of threads
                "spring.cloud.gcp.pubsub.executor.max-outbound-threads=100", // Adjust the number of threads
                "spring.integration.poller.max-messages-per-poll=5",
                "spring.integration.poller.fixed-delay=1000"
        }
)
public class SpacePublisherTest extends SpaceAbstractCommonTest {

    @Autowired
    private PubSubPublisher pubSubPublisher;
    @Autowired
    @Qualifier("fakePubSubTemplate")
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private PubSubServiceTest pubSubServiceTest;

    @Test
    void testPublishSuccessfully_ErrorFutureGet() throws Exception {

        pubSubServiceTest.createTopic("test-topic-"+ RequestContext.getRequestContextDetails().getDuId());
        Subscription subscription = pubSubServiceTest.subscribe("test-topic-"+ RequestContext.getRequestContextDetails().getDuId(), "subscribe-test");
        pubSubPublisher.publish("test-topic", "Hello, Pub/Sub!");

        Thread.sleep(1000);

        // Pull messages
        List<AcknowledgeablePubsubMessage> messages = pubSubTemplate.pull(subscription.getName(), 10, true);

        for (AcknowledgeablePubsubMessage message : messages) {
            System.out.println("Received message: " + message.getPubsubMessage());
            // Acknowledge the message
            pubSubTemplate.ack( List.of(message));
        }
    }

}