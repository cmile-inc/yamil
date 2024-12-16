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

import com.cmile.serviceutil.common.AuthUtil;
import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgMetricRegistryTest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {CfgMetricRegistryTest.class})
public class RestApiMetricFilterTest extends AbstractCommonTest {

    @Autowired
    private RestApiMetricsFilter restApiMetricsFilter;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        simpleMeterRegistry.clear();
    }

    @AfterEach
    public void tearDown() {
        simpleMeterRegistry.clear();
    }

    @Autowired
    private SimpleMeterRegistry simpleMeterRegistry;

    @Test
    public void restAPiMetricRegistryTest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + AuthUtil.jwtToken());
        String payload = "{\"key\":\"value\"}";
        request.setContent(payload.getBytes());
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Runnable runnable = () -> {
            response.setStatus(HttpServletResponse.SC_OK);
        };

        restApiMetricsFilter.doFilter(request, response, filterChain);
        assertNotNull(simpleMeterRegistry.getMeters());

    }
}
