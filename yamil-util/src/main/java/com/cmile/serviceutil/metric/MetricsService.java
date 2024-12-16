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

package com.cmile.serviceutil.metric;

import com.cmile.serviceutil.auth.RequestContext;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.google.pubsub.v1.PubsubMessage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetricsService {

  protected static final String DEFAULT_VALUE = "unknown";
  protected static final String API_ENDPOINT_TAG = "apiEndpoint";
  protected static final String METHOD_TAG = "httpMethod";
  public static final String DU_ID_TAG = "duId";
  public static final String SPACE_ID_TAG = "spaceId";
  protected static final String START_TIME = "startTime";
  public static final String EVENT_CHAIN_ORIGIN_TIME = "eventChainOriginTime";
  public static final String SERVICE_NAME_TAG = "serviceName";

  public static final String PROCESS_NAME = "processName";

  private final GCPServiceProject serviceProject;
  private final MeterRegistry meterRegistry;
  private final List<CustomTagExtractor> customTagsExtractors;

  public MetricsService(
      GCPServiceProject serviceProject,
      MeterRegistry meterRegistry,
      List<CustomTagExtractor> customTagsExtractors) {
    this.serviceProject = serviceProject;
    this.meterRegistry = meterRegistry;
    this.customTagsExtractors = customTagsExtractors;
  }

  public void recordMetric(
      MetricsType type, String name, long value, Map<String, String> customTags) {
    List<Tag> tags = getGenericProcessTags(name, customTags);
    type.registerMetrics(meterRegistry, tags, value);
  }

  public void wrapRestWithMetrics(
      HttpServletRequest request, HttpServletResponse response, Runnable method) {

    List<Tag> tags = getApiTags(request);
    long startTime = System.nanoTime(); // Capture start time

    try {
      MetricsType.REQUEST_PAYLOAD_SIZE.registerMetrics(
          meterRegistry, tags, request.getContentLengthLong());

      method.run();

      // Check for error HTTP status codes and record error metrics
      if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND
          || response.getStatus() == HttpServletResponse.SC_BAD_REQUEST
          || response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
        MetricsType.ERROR_COUNT.registerMetrics(meterRegistry, tags, 0);
      }

    } catch (Exception e) {
      // Record error metric in case of an exception
      MetricsType.ERROR_COUNT.registerMetrics(meterRegistry, tags, 0);
      throw new RuntimeException("Error during metric wrapping", e);

    } finally {
      // Record response time and size metrics
      long duration = System.nanoTime() - startTime;
      int responseSize = response.getBufferSize();
      MetricsType.REQUEST_PAYLOAD_SIZE.registerMetrics(meterRegistry, tags, responseSize);
      MetricsType.RESPONSE_TIME.registerMetrics(meterRegistry, tags, duration);
    }
  }

  public <T> T wrapMethodWithMetrics(
      String name, Supplier<T> method, Map<String, String> customTags) {
    long duration = 0;

    List<Tag> tags = getGenericProcessTags(name, customTags);
    Map<String, String> metricsMap = start();
    T result = null;
    try {
      result = method.get();
    } catch (Exception e) {
      MetricsType.ERROR_COUNT.registerMetrics(meterRegistry, tags, 0);
      throw e;
    } finally {
      duration = 0;

      try {
        long startTime = Long.parseLong(metricsMap.get("startTime"));
        duration = System.currentTimeMillis() - startTime;
      } catch (Exception e) {
        // Do Nothing
      }
      MetricsType.RESPONSE_TIME.registerMetrics(meterRegistry, tags, duration);
    }

    log.debug(
        "Recording {} metrics for: {}; with measured metric of : {}",
        name,
        customTags.entrySet().stream().map(Object::toString).collect(Collectors.joining(";")),
        duration);

    return result;
  }

  private List<Tag> getCustomTags(HttpServletRequest httpRequest) {
    List<Tag> tags = new ArrayList<>();
    if (this.customTagsExtractors != null && !this.customTagsExtractors.isEmpty()) {
      customTagsExtractors.stream()
          .map(
              extractor ->
                  Optional.ofNullable(extractor.extractTag(httpRequest))
                      .map(
                          val ->
                              Tag.of(
                                  extractor.getClass().getSimpleName(),
                                  extractor.extractTag(httpRequest))));
    }
    return tags;
  }

  private List<Tag> getApiTags(HttpServletRequest httpRequest) {
    List<Tag> tags = new ArrayList<>();
    tags.addAll(getCommonTags());
    tags.addAll(extractCustomTags(httpRequest));
    tags.add(
        Tag.of(
            API_ENDPOINT_TAG, httpRequest != null ? httpRequest.getRequestURI() : DEFAULT_VALUE));
    tags.add(Tag.of(METHOD_TAG, httpRequest != null ? httpRequest.getMethod() : DEFAULT_VALUE));
    List<Tag> customTags = getCustomTags(httpRequest);
    if (customTags != null && !customTags.isEmpty()) {
      tags.addAll(customTags);
    }
    return tags;
  }

  private List<Tag> getGenericProcessTags(String processName, Map<String, String> customTags) {
    if (processName == null) {
      throw new IllegalArgumentException("Process name expected for metrics");
    }
    List<Tag> tags = new ArrayList<>();
    tags.addAll(getCommonTags());

    if (customTags != null) {
      customTags.forEach(
          (key, value) -> tags.add(Tag.of(key, value != null ? value : DEFAULT_VALUE)));
    }
    tags.add(Tag.of(PROCESS_NAME, processName));
    return tags;
  }

  private List<Tag> getCommonTags() {
    String duId = null;
    String spaceId = null;

    if (RequestContext.getRequestContextDetails() != null) {
      duId = RequestContext.getRequestContextDetails().getDuId();
      spaceId = RequestContext.getRequestContextDetails().getSpaceId();
    }

    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of(SERVICE_NAME_TAG, serviceProject.getApplicationName()));
    tags.add(Tag.of(DU_ID_TAG, duId != null ? duId : DEFAULT_VALUE));
    tags.add(Tag.of(SPACE_ID_TAG, spaceId != null ? spaceId : DEFAULT_VALUE));
    return tags;
  }

  private List<Tag> extractCustomTags(HttpServletRequest httpServletRequest) {
    List<Tag> tags = new ArrayList<>();
    if (this.customTagsExtractors != null && !this.customTagsExtractors.isEmpty()) {
      customTagsExtractors.stream()
          .map(
              extractor ->
                  Optional.ofNullable(extractor.extractTag(httpServletRequest))
                      .map(
                          val ->
                              Tag.of(
                                  extractor.getClass().getSimpleName(),
                                  extractor.extractTag(httpServletRequest))));
    }
    return tags;
  }

  public Map<String, String> start() {
    Map<String, String> map = new HashMap<>();
    map.put(START_TIME, String.valueOf(System.currentTimeMillis()));
    return map;
  }

  public void registerEventChainOriginTime() {
    RequestContext.getRequestContextDetails()
        .getAdditionalInfo()
        .put(EVENT_CHAIN_ORIGIN_TIME, String.valueOf(System.currentTimeMillis()));
  }

  public Long propagateEventChainOriginTime(Map<String, String> pubSubAttributes) {
    Long eventChainOriginTime = 0L;
    try {
      eventChainOriginTime = Long.parseLong(pubSubAttributes.get(EVENT_CHAIN_ORIGIN_TIME));
    } catch (Exception e) {
      // Ignore the error. It is not mandatory to have Event Chain Origin Time
    }

    if (eventChainOriginTime > 0) {
      RequestContext.getRequestContextDetails()
          .getAdditionalInfo()
          .put(EVENT_CHAIN_ORIGIN_TIME, String.valueOf(eventChainOriginTime));
    }

    return eventChainOriginTime;
  }

  public void addEventChainOriginTime(PubsubMessage.Builder builder) {
    builder.putAttributes(EVENT_CHAIN_ORIGIN_TIME, String.valueOf(getEventChainOriginTime()));
  }

  private long getEventChainOriginTime() {

    Map<String, String> additionalInfo =
        RequestContext.getRequestContextDetails().getAdditionalInfo();
    String eventChainOriginTimeStr = null;
    if (additionalInfo != null) {
      eventChainOriginTimeStr = additionalInfo.get(EVENT_CHAIN_ORIGIN_TIME);
    }
    Long eventChainOriginTime = 0L;

    if (eventChainOriginTimeStr != null) {
      eventChainOriginTime = Long.parseLong(eventChainOriginTimeStr);
    } else {
      // If eventChainOriginTime is not set, set it to current time, it means that
      // this is a fresh event that is now starting. This is the origin time for this
      // event chain.
      eventChainOriginTime = System.currentTimeMillis();
    }
    return eventChainOriginTime;
  }

  private String getApplicationName() {
    String applicationName = RequestContext.getRequestContextDetails().getApplicationName();
    if (applicationName == null) {
      applicationName = "global";
    }
    return applicationName;
  }
}
