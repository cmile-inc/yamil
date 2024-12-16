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

import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgMetricRegistryTest;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.cumulative.CumulativeCounter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nishant-pentapalli
 */
@SpringBootTest(classes = CfgMetricRegistryTest.class)
public class MetricServiceTest extends AbstractCommonTest {
    @Autowired
    private MetricsService metricService;

    @Autowired
    private SimpleMeterRegistry simpleMeterRegistry;

    @BeforeEach
    public void setUp() {
      simpleMeterRegistry.clear();
    }
  
    @AfterEach
    public void tearDown() {
      simpleMeterRegistry.clear();
    }

    @Test
    public void testErrorCountMetric() {

        metricService.recordMetric(
                MetricsType.ERROR_COUNT, "testErrorCountMetric", 1, Map.of("name", "testErrorCountMetric"));
        List<Meter> meters = simpleMeterRegistry.getMeters();
        assertEquals(1, meters.size());
        CumulativeCounter cumulativeCounter = (CumulativeCounter) meters.get(0);
        assertEquals(1, cumulativeCounter.count());
    }

    @Test
    public void testResponseSizeMetric() {

        metricService.recordMetric(
                MetricsType.RESPONSE_PAYLOAD_SIZE, "testResponseSizeMetric", 1, Map.of("name", "testResponseSizeMetric"));
        List<Meter> meters = simpleMeterRegistry.getMeters();
        assertEquals(4, meters.size());
    }

    @Test
    public void testRequestSizeMetric() {

        metricService.recordMetric(
                MetricsType.REQUEST_PAYLOAD_SIZE, "testRequestSizeMetric", 1, Map.of("name", "testRequestSizeMetric"));
        List<Meter> meters = simpleMeterRegistry.getMeters();
        assertEquals(4, meters.size());
    }

    @Test
    public void testRequestTimeMetric() {

        metricService.recordMetric(
                MetricsType.RESPONSE_TIME, "testResponsetimeMetric", 1, Map.of("name", "testResponsetimeMetric"));
        List<Meter> meters = simpleMeterRegistry.getMeters();
        assertEquals(4, meters.size());
    }

    @Test
    public void testWrapMethodWithMetrics() {

        metricService.wrapMethodWithMetrics(
                "testWrapMethodWithMetrics",
                () -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "wrapMethodWithMetrics";
                },
                Map.of("name", "testWrapMethodWithMetrics"));

        List<Meter> meters = simpleMeterRegistry.getMeters();
        assertEquals(4, meters.size());
    }
}
