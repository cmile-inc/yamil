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

package com.cmile.serviceutil.apiinvoker;

import com.cmile.serviceutil.auth.CfgAuth;
import com.cmile.serviceutil.auth.jwt.JwtTokenProvider;
import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author mamtha.k
 */
@Configuration
@ComponentScan(basePackages = "com.cmile.serviceutil.apiinvoker")
@Import({CfgAuth.class, CfgGCPProject.class})
public class CfgApiInvoker {

  @Bean
  public ApiInvoker apiInvoker(
      GCPServiceProject gcpServiceProject, JwtTokenProvider jwtTokenProvider) {
    return new ApiInvoker(gcpServiceProject, jwtTokenProvider);
  }
}
