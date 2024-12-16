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

package com.cmile.serviceutil.spacevalidator;

import com.cmile.serviceutil.validators.space.SpaceCacheManager;
import com.cmile.serviceutil.validators.space.SpaceValidatorInterceptor;
import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgSpaceValidatorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = {CfgSpaceValidatorTest.class})
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
public class SpaceValidatorInterceptorGlobalScopeTest extends AbstractCommonTest {

    @Autowired
    private SpaceValidatorInterceptor spaceValidatorInterceptor;

    @Autowired
    private SpaceCacheManager spaceCacheManager;

    @Test
    public void testSpaceInterceptor_GlobalDoNotSetInterceptor() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();
        boolean res = spaceValidatorInterceptor.preHandle(request, response, handler);
        assertTrue(res);
    }


}
