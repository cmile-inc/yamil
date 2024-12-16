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

package com.cmile.serviceutil.job.pubsub;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.job.PubSub.PubSubPublisher;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.serviceutil.util.ServiceUtilConstants;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PubSubPublisherTest {

    private static final String testMessageInput = "any message";
    private static final String topicName = "any-cooked-up-topic";
    private static final String expectedMessageId = "mock-message-id";

    @Mock
    private PubSubTemplate pubSubTemplate;

    @InjectMocks
    private PubSubPublisher publisher;

    @Mock
    private MetricsService metricsService;

    @Test
    public void testPublishMessage() throws Exception {
        // Set up RequestContext to avoid nulls
        ContextInfo ctx = new ContextInfo();
        ctx.setCorrelationId("1234567890");
        ctx.setDuId("anyDuId");
        ctx.setSpaceId("anySpaceId");
        RequestContext.setRequestContextDetails(ctx);

        // Create a completed CompletableFuture with a mock message ID
        CompletableFuture<String> mockFuture = CompletableFuture.completedFuture(expectedMessageId);

        // Stub pubSubTemplate.publish() with a valid future response
        doReturn(mockFuture).when(pubSubTemplate).publish(anyString(), any(PubsubMessage.class));

        // Call the method under test
        String actualMessageId = publisher.publish(topicName, testMessageInput);

        // Capture the PubsubMessage that was sent
        ArgumentCaptor<PubsubMessage> messageCaptor = ArgumentCaptor.forClass(PubsubMessage.class);
        verify(pubSubTemplate, times(1)).publish(eq(topicName + "-anyDuId"), messageCaptor.capture());

        // Validate the results
        assertEquals(expectedMessageId, actualMessageId);
        PubsubMessage capturedMessage = messageCaptor.getValue();
        assertEquals(testMessageInput, capturedMessage.getData().toStringUtf8());
        assertEquals("anySpaceId", capturedMessage.getAttributesMap().get(MetricsService.SPACE_ID_TAG));
        assertEquals("1234567890", capturedMessage.getAttributesMap().get( ServiceUtilConstants.CORRELATION_ID));
    }
}
