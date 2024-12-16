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

import com.cmile.testutil.CfgMetricRegistryTest;
import com.cmile.testutil.CfgPubSubTest;
import com.cmile.testutil.pubsub.PubSubServiceTest;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;

@Configuration
        @Import({CfgPubSubTest.class, CfgMetricRegistryTest.class, CfgPubSubTest.class})
public class CfgTestPubSubStimulate {

    private String topic = "test-topic";
    private final String SUBSCRIPTION_SEPERATOR = "-sub";

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private PubSubServiceTest pubSubServiceTest;
    @Bean("testActivator")
    @Primary
    public MessageChannel frIngestionInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter frIngestionInboundChannelAdapter(
            @Qualifier("testActivator") MessageChannel messageChannel,
            PubSubTemplate pubSubTemplate) throws IOException {
        pubSubServiceTest.createTopic(topic);
        pubSubServiceTest.subscribe(topic, topic+"-sub");
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, topic+"-sub");
        adapter.setOutputChannel(messageChannel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }
}
