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

import com.cmile.serviceutil.logging.CfgLogging;
import com.cmile.serviceutil.logging.LogFilter;
import com.cmile.testutil.AbstractCommonTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {CfgLogging.class})
class CfgLoggingTest extends AbstractCommonTest {

    @Autowired
    private CfgLogging cfgLogging;

    @BeforeEach
    public void setUp() {
    }


    @Test
    void testLogFilter() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));

        FilterChain chain = mock(FilterChain.class);
        LogFilter logFilter = cfgLogging.logFilter().getFilter();

        // Act
        logFilter.doFilter(request, mock(ServletResponse.class), chain);

        // Assert
        assertNotNull(logFilter);
        assertEquals(4, cfgLogging.logFilter().getOrder());
        assertEquals("/*", cfgLogging.logFilter().getUrlPatterns().iterator().next());

    }


}
