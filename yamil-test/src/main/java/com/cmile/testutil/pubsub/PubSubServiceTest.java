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

package com.cmile.testutil.pubsub;

import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PubSubServiceTest {

    @Autowired
    @Qualifier("fakePubSubTemplate")
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private TransportChannelProvider channelProvider;

    @Autowired
    private GCPServiceProject gcpServiceProject;

    public Topic createTopic(String topicName) throws IOException {
        Topic topic = null;
        TopicAdminSettings topicAdminSettings = TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build();
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
            TopicName topicNameConfig = TopicName.of(gcpServiceProject.getProjectId(), topicName);
            topic = topicAdminClient.createTopic(topicNameConfig);
        }
        return topic;
    }

    public Subscription subscribe(String topicName, String subscriptionName) throws IOException {
        Subscription subscription = null;
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build();
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
            SubscriptionName subscriptionNameCfg = SubscriptionName.of(gcpServiceProject.getProjectId(), subscriptionName);
            TopicName topicNameCfg = TopicName.of(gcpServiceProject.getProjectId(), topicName);

            subscription = subscriptionAdminClient.createSubscription(
                    subscriptionNameCfg,
                    topicNameCfg,
                    PushConfig.getDefaultInstance(),
                    10  // ack deadline in seconds
            );
        }
        return subscription;
    }

}