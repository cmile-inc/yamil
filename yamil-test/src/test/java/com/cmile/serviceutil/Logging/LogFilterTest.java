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

package com.cmile.serviceutil.Logging;

import com.cmile.serviceutil.logging.LogFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFilterTest {

    private LogFilter logFilter;
    private Map<String, Function<HttpServletRequest, String>> mdcParams;

    @BeforeEach
    void setUp() {
        mdcParams = new HashMap<>();
        mdcParams.put("userId", request -> "testUser");
        mdcParams.put("requestedUrl", request -> "http://localhost/test");
        logFilter = new LogFilter(mdcParams);
    }

    @Test
    void testDoFilter() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));
        
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        assertDoesNotThrow(() -> logFilter.doFilter(request, response, chain), "doFilter should not throw an exception");

    }

}
