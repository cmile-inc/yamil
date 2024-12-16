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

import com.cmile.serviceutil.common.CfgCommon;
import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.cmile.serviceutil.secret.CfgSecret;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CfgCommon.class, CfgSecret.class, CfgGCPProject.class})
@ComponentScan(basePackages = "com.cmile.serviceutil.auth")
public class CfgAuth {

  private static final Logger logger = LoggerFactory.getLogger(CfgAuth.class);

  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> authenticationFilter(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      CompositeAuthorizationBypassStrategy compositeAuthorizationBypassStrategy) {

    FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
        new FilterRegistrationBean<JwtAuthenticationFilter>();
    registrationBean.setFilter(jwtAuthenticationFilter);
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(1);
    return registrationBean;
  }

  @Bean
  public FirebaseApp firebaseApp(GCPServiceProject gcpServiceProject) {
    if (!FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.getInstance();
    }
    logger.debug(
        "Initializing Firebase App for service {} in project: {}",
        gcpServiceProject.getApplicationName(),
        gcpServiceProject.getProjectId());
    FirebaseOptions options =
        FirebaseOptions.builder().setCredentials(gcpServiceProject.getGoogleCredentials()).build();
    return FirebaseApp.initializeApp(options);
  }

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    return FirebaseAuth.getInstance(firebaseApp);
  }
}
