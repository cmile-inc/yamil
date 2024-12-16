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

package com.cmile.testutil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.cmile.serviceutil.apiinvoker.CfgApiInvoker;
import com.cmile.serviceutil.auth.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({CfgSecretTest.class, CfgWireMockServerTest.class, CfgApiInvoker.class})
public class CfgApiInvokerTest {

  @Bean("mockedJwtProvider")
  @Primary
  public JwtTokenProvider jwtTokenProvider(@Autowired JwtTokenProvider realJwtTokenProvider) {
    JwtTokenProvider mockedJwtTokenProvider = spy(realJwtTokenProvider);
    when(mockedJwtTokenProvider.createToken(
            any(), anyString(), anyString(), any(), anyString(), anyString()))
        .thenReturn(serviceClientJwtToken());
    return mockedJwtTokenProvider;
  }

  private String serviceClientJwtToken() {
    return "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxZjFkZTY1YS01NjI0LTRjYzMtYWViNy1hNzRiMTNlZDk4NzQiLCJpc3MiOiJodHRwczovL2FwaS5jbWlsZS5jb20vdG9rZW4vIiwiaWF0IjoxNzI5ODMzMTA1LCJleHAiOjE3Mjk4MzY3MDV9.DuYyehXmCMpsuXMkeza7GX5twSKsCqWrZB1OhGkyfXc7Q2pFk3DBwLC6JaskhO15MQJsSM2b9UWRe1Lqy7az8DS7nWs-oQov-FOWNmsOu0aqGeG1rrRjp1erY4Y50qNoFzsm6VUCfpeoHyImvCiydL21wkou8vyXSrMWWirwekWSBEo99r-Q-dU3gDxDXP7IJTu-lZSKuWPKwzTpOV08CePASe8j1GmtzpEOGt2mOw8w7DgIBZCu2LqWjPbrVEEcqCyUbyQnHj8VQMjj3e5WFyMI8Zf40HDUUmL8x3E_5L-SdVNtYLnKXVR233erc36L85bRUCg2lS3WsqDsCfNWkA";
  }
}
