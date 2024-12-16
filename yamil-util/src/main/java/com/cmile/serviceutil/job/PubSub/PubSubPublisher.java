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

package com.cmile.serviceutil.job.PubSub;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.metric.MetricsService;
import com.cmile.serviceutil.metric.MetricsType;
import com.cmile.serviceutil.util.ServiceUtilConstants;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class PubSubPublisher extends AbstractPubSubHandler {

  private static final String METRIC_NAME = "publishPubSubMessage";

  public PubSubPublisher(PubSubTemplate pubSubTemplate, MetricsService metricsService, GCPServiceProject gcpServiceProject) {
    super(pubSubTemplate, metricsService, gcpServiceProject);
  }

  public String publish(String topicPrefix, String message) throws Exception {
    String messageId = null;

    Map<String, String> metricsMap = metricsService.start();

    try {
      messageId = actuallyPublish(topicPrefix, message);
    } catch (InterruptedException ex) {
      log.debug(
          "PubSub Publisher: Failed to publish message to topic: {}, for spaceId: {}, due to exception: {}",
          getTopicName(topicPrefix),
          extractSpaceId(),
          ex.getMessage());
      metricsService.recordMetric(MetricsType.ERROR_COUNT, METRIC_NAME, 0, null);
      throw ex;
    } catch (ExecutionException ex) {
      log.debug(
          "PubSub Publisher: Failed to publish message to topic: {}, for spaceId: {}, due to exception: {}",
          getTopicName(topicPrefix),
          extractSpaceId(),
          ex.getMessage());
      throw ex;
    } finally {
      String startTimeStr = metricsMap.get("startTime");
      if (startTimeStr != null) {
        long startTime = Long.parseLong(startTimeStr);
        long duration = System.currentTimeMillis() - startTime;
        metricsService.recordMetric(MetricsType.RESPONSE_TIME, METRIC_NAME, duration, metricsMap);
      }
    }

    return messageId;
  }

  private String actuallyPublish(String topicPrefix, String message) throws Exception {

    PubsubMessage.Builder pubsubMessageBuilder = PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8(message))
        .putAttributes(MetricsService.SPACE_ID_TAG, extractSpaceId())
        .putAttributes( ServiceUtilConstants.CORRELATION_ID, extractCorrelationId());

    metricsService.addEventChainOriginTime(pubsubMessageBuilder);

    pubsubMessageBuilder.build();

    PubsubMessage pubsubMessage = pubsubMessageBuilder.build();

    CompletableFuture<String> future = getPubSubTemplate().publish(getTopicName(topicPrefix), pubsubMessage);

    String messageId = future.get();
    log.debug(
        "PubSub Publisher: Message published successfully with messageId: {}, to topic: {}, for spaceId: {}, with payload: {}",
        messageId,
        getTopicName(topicPrefix),
        extractSpaceId(),
        message);

    return messageId;
  }

  private String getTopicName(String topicPrefix) {
    return topicPrefix + "-" + extractDuId();
  }

  private String extractSpaceId() {
    String spaceId = getContext().getSpaceId();
    if (spaceId != null) {
      String mdcSpaceId = MDC.get(MetricsService.SPACE_ID_TAG);
      if (mdcSpaceId == null) {
        MDC.put(MetricsService.SPACE_ID_TAG, spaceId);
      }
    } else {
      spaceId = MDC.get(MetricsService.SPACE_ID_TAG);
      if (spaceId == null) {
        spaceId = "global";
        MDC.put(MetricsService.SPACE_ID_TAG, spaceId);
      }
      getContext().setSpaceId(spaceId);
    }
    return spaceId;
  }

  private ContextInfo getContext() {
    ContextInfo ctx = RequestContext.getRequestContextDetails();
    if (ctx == null) {
      throw new RuntimeException("Context is not initialized.  Please initialize context.");
    }

    return ctx;
  }

  private String extractDuId() {
    return getContext().getDuId();
  }

  private String extractCorrelationId() {
    String correlationId = getContext().getCorrelationId();
    if (correlationId == null) {
      correlationId = MDC.get( ServiceUtilConstants.CORRELATION_ID);
    } else {
      // self healing system. If for any reason MDC doesn't have the correlation Id,
      // this is the place to fix it.
      if (MDC.get( ServiceUtilConstants.CORRELATION_ID) == null) {
        MDC.put( ServiceUtilConstants.CORRELATION_ID, correlationId);
      }
    }
    if (correlationId == null) {
      // self healing system. If you don't provide a correlationId, we will generate
      // one for you.
      correlationId = UUID.randomUUID().toString();
      RequestContext.getRequestContextDetails().setCorrelationId(correlationId);
      MDC.put( ServiceUtilConstants.CORRELATION_ID, correlationId);
    }
    return correlationId;
  }
}
