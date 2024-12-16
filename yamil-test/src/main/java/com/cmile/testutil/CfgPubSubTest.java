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

import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.job.PubSub.PubSubPublisher;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.testutil.pubsub.PubSubServiceTest;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.core.PubSubConfiguration;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.DefaultPublisherFactory;
import com.google.cloud.spring.pubsub.support.DefaultSubscriberFactory;
import com.google.cloud.spring.pubsub.support.PublisherFactory;
import com.google.cloud.spring.pubsub.support.SubscriberFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

@Import({CfgGCPProject.class, PubSubServiceTest.class})
public class CfgPubSubTest {
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
    @Bean
    public PubSubEmulatorContainer pubSubContainer() {
        PubSubEmulatorContainer pubsubEmulator =
                new PubSubEmulatorContainer(DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators"));
        pubsubEmulator.start();
        System.setProperty("spring.cloud.gcp.pubsub.emulator-host", pubsubEmulator.getEmulatorEndpoint());
        System.setProperty("PUBSUB_EMULATOR_HOST", pubsubEmulator.getEmulatorEndpoint());
        return pubsubEmulator;
    }

    @Bean(name = "mockedChannel")
    @Primary
    public ManagedChannel managedChannel(PubSubEmulatorContainer pubSubEmulatorContainer) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(pubSubEmulatorContainer.getHost(), pubSubEmulatorContainer.getMappedPort(8085)).usePlaintext().build();

        return channel;
    }

    @Bean
    @Primary
    public TransportChannelProvider transportChannelProvider(ManagedChannel channel) {
        TransportChannelProvider channelProvider =
                FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        return channelProvider;

    }

    @Bean
    @Primary
    public TopicAdminClient topicAdminClient(TransportChannelProvider channelProvider,
                                             PubSubEmulatorContainer pubSubEmulatorContainer, GCPServiceProject gcpServiceProject) throws IOException {
        return TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setCredentialsProvider(credentialsProvider)
                        .setTransportChannelProvider(channelProvider)
                        .setEndpoint(pubSubEmulatorContainer.getEmulatorEndpoint()) // Set the emulator endpoint here
                        .build());
    }


    @Bean
    @Primary
    public SubscriptionAdminClient subscriptionAdminClient(TransportChannelProvider channelProvider, GCPServiceProject gcpServiceProject) throws IOException {
        SubscriptionAdminClient subscriptionAdminClient =
                SubscriptionAdminClient.create(
                        SubscriptionAdminSettings.newBuilder()
                                .setTransportChannelProvider(channelProvider)
                                .setCredentialsProvider(credentialsProvider)
                                .build());
        return subscriptionAdminClient;
    }

    @Bean
    @DependsOn("fakePubSubTemplate")
    public PubSubPublisher pubSubPublisher(PubSubTemplate pubSubTemplate, MetricsService metricsService, GCPServiceProject gcpServiceProject) {
        return new PubSubPublisher(pubSubTemplate, metricsService, gcpServiceProject);
    }

    @Bean("fakePubSubTemplate")
    @Primary
    public PubSubTemplate pubSubTemplate(PublisherFactory publisherFactory, SubscriberFactory subscriberFactory) {
        return new PubSubTemplate(publisherFactory, subscriberFactory);
    }

    @Bean
    public MessageChannel myInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @Primary
    public PublisherFactory publisherFactory(GcpProjectIdProvider gcpProjectIdProvider,
                                             TransportChannelProvider channelProvider) throws IOException {
        DefaultPublisherFactory publisherFactory = new DefaultPublisherFactory(gcpProjectIdProvider);
        publisherFactory.setChannelProvider(channelProvider);
        publisherFactory.setCredentialsProvider(credentialsProvider);
        return publisherFactory;
    }


    @Bean
    @Primary
    public SubscriberFactory subscriberFactory(GcpProjectIdProvider gcpProjectIdProvider,
                                               TransportChannelProvider channelProvider) {

        PubSubConfiguration configuration = new PubSubConfiguration();
        configuration.initialize(gcpProjectIdProvider.getProjectId());
        DefaultSubscriberFactory defaultSubscriberFactory = new DefaultSubscriberFactory(gcpProjectIdProvider,
                configuration
        );
        defaultSubscriberFactory.setCredentialsProvider(credentialsProvider);
        defaultSubscriberFactory.setChannelProvider(channelProvider);
        return defaultSubscriberFactory;
    }
}