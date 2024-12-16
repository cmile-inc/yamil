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

package com.cmile.serviceutil.auth;

import com.cmile.serviceutil.auth.jwt.*;
import com.cmile.serviceutil.common.AuthUtil;
import com.cmile.testutil.AbstractCommonTest;
import com.cmile.testutil.CfgAuthTest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.impl.DefaultJwtParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CfgAuthTest.class})
@TestPropertySource(
        properties = {"gcp.service.account.key.file.path=testutil/test-credentials.json"})
public class JwtAuthenticationFilterTest extends AbstractCommonTest {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilterTest.class);

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserAuthTokenVerifier userAuthTokenVerifier;

    @Autowired
    private SpaceClientTokenVerifier spaceClientTokenVerifier;

    @Mock
    private FilterChain filterChain;

    @MockBean
    private FirebaseAuth mockFirebaseAuth;

    @MockBean
    private DefaultJwtParser defaultJwtParser;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        logger.debug("Setting up the test");
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() {
        logger.debug("Tearing down the test");
    }

    @Test
    public void testUserAuthTokenVerifier_success() throws ServletException, IOException, FirebaseAuthException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + AuthUtil.jwtToken());
        UserAuthTokenVerifier mockUserAuthTokenVerifier = spy(userAuthTokenVerifier);
        when(mockUserAuthTokenVerifier.verifyToken(AuthUtil.jwtToken())).thenReturn(AuthUtil.userTokenVErifyResult());

        FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);
        when(mockFirebaseToken.getClaims()).thenReturn(AuthUtil.userTokenVErifyResult().getClaims());

        when(mockFirebaseAuth.verifyIdToken(anyString())).thenReturn(mockFirebaseToken);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        ContextInfo context = RequestContext.getRequestContextDetails();
        assertNotNull(context);

    }

    @Test
    public void testUserAuthTokenVerifier_UnauthorizedException() throws ServletException, IOException, FirebaseAuthException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + AuthUtil.jwtToken());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }


    @Test
    public void testUserAuthTokenVerifier_ExceptionBearerTokenRequired() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testSpaceClientTokenVerifier() throws ServletException, IOException {
        // Setup mock request and response
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + AuthUtil.serviceClientJwtToken());
        jwtAuthenticationFilter.doFilter(request, response, filterChain);
    }


    @Test
    public void testServiceClientTokenVerifier() throws ServletException, IOException {
        // Setup mock request and response
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
       String token =  jwtTokenProvider.createToken(ClientScope.SERVICE, "test", "test",
               "test", "test", "test");
        request.addHeader("Authorization", "Bearer " + token);
        jwtAuthenticationFilter.doFilter(request, response, filterChain);
        ContextInfo context = RequestContext.getRequestContextDetails();
        assertNotNull(context);
    }


}
