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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.concurrent.TimeUnit;

public enum MetricsType {
  RESPONSE_TIME("response_time") {
    @Override
    public void registerMetrics(MeterRegistry meterRegistry, List<Tag> tags, long value) {
      Timer.builder(metricName)
          .tags(tags)
          .publishPercentileHistogram(true)
          .publishPercentiles(0.5, 0.95, 0.99)
          .register(meterRegistry)
          .record(value, TimeUnit.NANOSECONDS);
    }
  },
  RESPONSE_PAYLOAD_SIZE("response_payload_size") {
    @Override
    public void registerMetrics(MeterRegistry meterRegistry, List<Tag> tags, long value) {
      DistributionSummary.builder(metricName)
          .tags(tags)
          .publishPercentileHistogram(true)
          .publishPercentiles(0.5, 0.95, 0.99)
          .register(meterRegistry)
          .record(value);
    }
  },
  ERROR_COUNT("error_count") {
    @Override
    public void registerMetrics(MeterRegistry meterRegistry, List<Tag> tags, long value) {
      Counter.builder(metricName).tags(tags).register(meterRegistry).increment();
    }
  },
  REQUEST_PAYLOAD_SIZE("request_payload_size") {
    @Override
    public void registerMetrics(MeterRegistry meterRegistry, List<Tag> tags, long value) {
      DistributionSummary.builder(metricName)
          .tags(tags)
          .publishPercentileHistogram(true)
          .publishPercentiles(0.5, 0.95, 0.99)
          .register(meterRegistry)
          .record(value);
    }
  };

  protected final String metricName;

  MetricsType(String metricName) {
    this.metricName = metricName;
  }

  public abstract void registerMetrics(MeterRegistry meterRegistry, List<Tag> tags, long value);
}
