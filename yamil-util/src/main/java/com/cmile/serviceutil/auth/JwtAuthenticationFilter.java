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

import com.cmile.serviceutil.auth.jwt.ClientScope;
import com.cmile.serviceutil.auth.jwt.ContextInfo;
import com.cmile.serviceutil.auth.jwt.JwtVerificationService;
import com.cmile.serviceutil.auth.jwt.TokenVerificationResult;
import com.cmile.serviceutil.gcp.ApplicationScopeEnum;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.util.ServiceUtilConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Filter to authenticate requests using JWT tokens
 *
 * @author nishant-pentapalli
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements Filter {
    private final JwtVerificationService jwtVerificationService;
    private final CompositeAuthorizationBypassStrategy compositeAuthorizationBypassStrategy;
    private final Map<String, Function<HttpServletRequest, String>> paramExtractors;
    private final GCPServiceProject gcpServiceProject;

    @Autowired
    public JwtAuthenticationFilter(
            JwtVerificationService jwtVerificationService,
            CompositeAuthorizationBypassStrategy compositeAuthorizationBypassStrategy,
            Map<String, Function<HttpServletRequest, String>> paramExtractors,
            GCPServiceProject gcpServiceProject) {
        this.jwtVerificationService = jwtVerificationService;
        this.compositeAuthorizationBypassStrategy = compositeAuthorizationBypassStrategy;
        this.paramExtractors = paramExtractors;
        this.gcpServiceProject = gcpServiceProject;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        log.debug("Auth Filter: entering doFilter");

        if (compositeAuthorizationBypassStrategy.shouldBypass(httpRequest)) {
            ContextInfo context = new ContextInfo();
            extractSpaceId(context, httpRequest);
            extractCorrelationId(context, httpRequest);
            extractOtherParams(context, httpRequest);
            setDuId(context);
            RequestContext.setRequestContextDetails(context);
            chain.doFilter(request, response);
            return;
        }

        // Get the Authorization header
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Extract token from "Bearer " prefix

            try {
                // Verify the token using JwtVerificationService
                TokenVerificationResult result = jwtVerificationService.verifyToken(token);

                if (result.isValid()) {
                    // Set the claims in the request for downstream usage
                    ContextInfo context = ClientScope.fromClaims(result.getClaims());
                    extractSpaceId(context, httpRequest);
                    extractCorrelationId(context, httpRequest);
                    extractOtherParams(context, httpRequest);
                    setDuId(context);

                    RequestContext.setRequestContextDetails(context);

                    // Optionally, set the authenticated user context here
                    // For example, you can set the user principal in the security context
                } else {
                    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
            } catch (Exception e) {
                // Handle exceptions during token verification
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        } else {
            // No Authorization header, or it does not start with "Bearer "
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        log.debug("Auth Filter: forwarding request within doFilter");
        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
        RequestContext.clear();
    }

    private void extractSpaceId(ContextInfo context, HttpServletRequest httpRequest) {

        if (ApplicationScopeEnum.GLOBAL.name().equalsIgnoreCase(
                gcpServiceProject.getAppDeploymentScope())) {
            context.setSpaceId(gcpServiceProject.getApplicationName().split("-")[1]);
        } else {
            String[] segments = httpRequest.getRequestURI().split("/");
            if (segments.length > 1) {
                context.setSpaceId(segments[1]);
            }
        }

    }

    private void extractOtherParams(ContextInfo context, HttpServletRequest httpRequest) {

        if (paramExtractors == null) {
            return;
        }

        Map<String, String> paramsMap = new HashMap<>();

        try {
            paramExtractors.forEach(
                    (key, extractor) -> {
                        String val = extractor.apply(httpRequest);
                        paramsMap.put(key, val);
                    });
        } catch (Exception e) {
            System.err.println("Error extracting parameters: " + e.getMessage());
        }

        if (paramsMap != null && !paramsMap.isEmpty()) {
            context.getAdditionalInfo().putAll(paramsMap);
        }
    }

    private void extractCorrelationId(ContextInfo context, HttpServletRequest httpRequest) {
        String correlationId = httpRequest.getHeader(ServiceUtilConstants.CORRELATION_ID);
        if (StringUtils.isBlank(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        context.setCorrelationId(correlationId);
    }

    private void setDuId(ContextInfo context) {
        context.setDuId(gcpServiceProject.getDu());
    }
}
