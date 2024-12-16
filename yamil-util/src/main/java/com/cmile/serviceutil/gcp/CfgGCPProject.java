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

package com.cmile.serviceutil.gcp;

import com.cmile.serviceutil.common.CfgCommon;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CfgCommon.class})
@ComponentScan(basePackages = "com.cmile.serviceutil.gcp")
public class CfgGCPProject {
  @Value("${PROJECT_ID:${gcp.service.project:cmile-central}}")
  private String projectId;

  @Bean
  public GcpProjectIdProvider gcpProjectIdProvider() {
    return () -> projectId;
  }
}
