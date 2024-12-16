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
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.protobuf.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractPubSubHandler {

  protected final PubSubTemplate pubSubTemplate;
  protected final MetricsService metricsService;
  protected final GCPServiceProject gcpServiceProject;

  public AbstractPubSubHandler(
      PubSubTemplate pubSubTemplate,
      MetricsService metricsService,
      GCPServiceProject gcpServiceProject) {
    this.pubSubTemplate = pubSubTemplate;
    this.metricsService = metricsService;
    this.gcpServiceProject = gcpServiceProject;
  }

  protected PubSubTemplate getPubSubTemplate() {
    return pubSubTemplate;
  }

  public void init(BasicAcknowledgeablePubsubMessage message) {
    try {
      Map<String, String> attributes = message.getPubsubMessage().getAttributesMap();
      String spaceId = extractSpaceId(attributes);
      String correlationId = extractCorrelationId(attributes);
      Timestamp messagePublishTime = message.getPubsubMessage().getPublishTime();
      ContextInfo ctx = new ContextInfo();
      ctx.setSpaceId(spaceId);
      ctx.setCorrelationId(correlationId);
      ctx.setDuId(gcpServiceProject.getDu());
      ctx.setApplicationName(gcpServiceProject.getApplicationName());
      RequestContext.setRequestContextDetails(ctx);
      // Set MDC for logging
      MDC.put(MetricsService.SPACE_ID_TAG, spaceId);
      MDC.put(ServiceUtilConstants.CORRELATION_ID, correlationId); // Not generating it

      Long eventChainOriginTime = metricsService.propagateEventChainOriginTime(attributes);

      recordEventLatencies(
          message.getProjectSubscriptionName().getSubscription(),
          eventChainOriginTime,
          messagePublishTime);

    } catch (Exception e) {
      log.error("Error setting Context and MDC: " + e.getMessage(), e);
    }
  }

  private String extractSpaceId(Map<String, String> attributes) {
    String spaceId =
        Optional.ofNullable(attributes.get(MetricsService.SPACE_ID_TAG)).orElse("global");
    return spaceId;
  }

  private String extractCorrelationId(Map<String, String> attributes) {
    String correlationId =
        Optional.ofNullable(attributes.get("X-Correlation-ID"))
            .orElse(UUID.randomUUID().toString());
    return correlationId;
  }

  private void recordEventLatencies(
      String name, Long eventChainOriginTime, Timestamp messagePublishTime) {
    long currentTime = System.currentTimeMillis();

    if (eventChainOriginTime > 0) {
      long eventLatency = currentTime - eventChainOriginTime;
      metricsService.recordMetric(
          MetricsType.RESPONSE_TIME, name + "-event-chain-", eventLatency, null);
    }

    if (messagePublishTime != null) {
      // Seems like the messagePublishTime is in seconds, so we need to multiply by
      // 1000 to get milliseconds. Also not sure if there is some round down. So add
      // one second just to make sure there are no negative latencies.
      Long messageLatency = currentTime - (messagePublishTime.getSeconds() + 1) * 1000;
      metricsService.recordMetric(
          MetricsType.RESPONSE_TIME, name + "-event-chain-", messageLatency, null);
    }
  }

  public void clear() {
    RequestContext.clear();
    MDC.clear();
  }
}
