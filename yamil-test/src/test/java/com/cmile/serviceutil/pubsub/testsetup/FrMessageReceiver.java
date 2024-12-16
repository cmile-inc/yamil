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

package com.cmile.serviceutil.pubsub.testsetup;

import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.job.PubSub.AbstractPubSubHandler;
import com.cmile.serviceutil.job.PubSub.PubSubPublisher;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.testutil.CfgMetricRegistryTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertEquals;

@Component
        @Import({CfgTestPubSubStimulate.class, CfgMetricRegistryTest.class, CfgGCPProject.class})
public class FrMessageReceiver extends AbstractPubSubHandler {

    private static final Logger logger = LoggerFactory.getLogger(FrMessageReceiver.class);


    @Qualifier(value = "testActivator")
    private final MessageChannel messageChannel;
    private final ObjectMapper objectMapper;
    private final  PubSubPublisher publisher;
    private final PubSubTemplate pubSubTemplate;
    private final GCPServiceProject gcpServiceProject;
    private final MetricsService metricsService;

    private String topic = "fr-ingest-DU01";
    private final String SUBSCRIPTION_SEPERATOR = "-sub";
    public FrMessageReceiver(MessageChannel messageChannel, ObjectMapper objectMapper, PubSubPublisher publisher,
                             PubSubTemplate pubSubTemplate, GCPServiceProject gcpServiceProject, MetricsService metricsService) {
        super(pubSubTemplate, metricsService, gcpServiceProject);
        this.messageChannel = messageChannel;
        this.objectMapper = objectMapper;
        this.publisher = publisher;
        this.pubSubTemplate = pubSubTemplate;
        this.gcpServiceProject = gcpServiceProject;
        this.metricsService = metricsService;
    }

    @ServiceActivator(inputChannel = "testActivator")
    public void testActivator(
            String payload,
            @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {

        try {
            String payloadSample = "{\"sampleField\":\"sampleValue\"}";
            logger.debug("Processing Start: {}.", payload);
            assertEquals(payloadSample.toString(), payload.toString());
            message.ack();
            logger.debug("Processing End, Message Acked: {}.", payload);

        } catch (Exception e) {
            message.nack();
            logger.error("Processing End, Message Nacked: {} due to exception: {}", payload, e.getMessage());
            e.printStackTrace();
        }
    }

    public void simulateMessageSend(String payload) {
        messageChannel.send(MessageBuilder.withPayload(payload).build());
    }

}
