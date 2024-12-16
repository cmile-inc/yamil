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

package com.cmile.serviceutil.common;

import com.cmile.serviceutil.auth.jwt.TokenVerificationResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

import java.util.HashMap;
import java.util.Map;

public class AuthUtil {

    public static TokenVerificationResult userTokenVErifyResult() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", "test");
        claims.put("auth_time", 1726133376L);
        claims.put("exp", 1729708566L);
        claims.put("iat", 11386010894L);
        claims.put("iss", "https://securetoken.google.com/test");
        claims.put("sub", "UVtc8KrAB0bS4IAzGsbhmGmknU82");
        claims.put("name", "test");
        claims.put("cmileUserId", "test");
        claims.put("idpUserId", "test");
        claims.put("user_id", "test");
        claims.put("email", "test@test.com");
        claims.put("email_verified", true);
        claims.put("firebase", Map.of("sign_in_provider", "google.com", "tenant", "test_tenant"));

        Claims claim =  new DefaultClaims(claims);
        return new TokenVerificationResult(true, claim);
    }

    public static TokenVerificationResult spaceTokenVErifyResult() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", "test");
        claims.put("auth_time", 1726133376L);
        claims.put("exp", 1729708566L);
        claims.put("iat", 11386010894L);
        claims.put("iss", "https://api.cmile.com/token/");
        claims.put("sub", "UVtc8KrAB0bS4IAzGsbhmGmknU82");
        claims.put("name", "test");
        claims.put("sid", "space1");
        claims.put("duid", "DU1");
        Claims claim =  new DefaultClaims(claims);
        return new TokenVerificationResult(true, claim);
    }


    public static String jwtToken() {
        return  "eyJhbGciOiJSUzI1NiIsImtpZCI6IjcxOGY0ZGY5MmFkMTc1ZjZhMDMwN2FiNjVkOGY2N2YwNTRmYTFlNWYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZHVtbXktZGV2LWR1bW15IiwiaWF0IjoxNzI5NzQzNzIxLCJleHAiOjIzODUxMDgxMzIxLCJhdWQiOiJkdW1teS1kZXYtZHVtbXkiLCJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwibmFtZSI6InRlc3QiLCJjbWlsZVVzZXJJZCI6InRlc3QiLCJpZHBVc2VySWQiOiJ0ZXN0IiwidXNlcmlkIjoidGVzdCJ9.k_G-i_-iF4owMIsX-LGvIybdt4I0vIQ2JVDGEeNlKKbe2xmLbLJzOPrw6p9MQexvaRd2gSy3e6aOpZuynrw29eO0-2SEPzkKor91QMNMxIUygbFajbHOJOAF_8ozpL6d8krovRxFh6I9oLZ12tXtzdNlmsWeQqKTWFUqG8TOHibKP0Xx_g6e48ojkCxS-ZBcw0Mss3_pmPebHmG2IkTPYdy2HHhcLOW40AuVjCxHLZMCaxEJi-P698IXm449EmwpIsZWZMavljNrZqbnvlRk-CTPgE3V-nAA89WWNz96NZyhUWS6N4TfFeUJdIA75dHHLJqEEHuRCgxyCEAI-4UkKA";

    }

    public static String serviceClientJwtToken() {
        return "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxZjFkZTY1YS01NjI0LTRjYzMtYWViNy1hNzRiMTNlZDk4NzQiLCJpc3MiOiJodHRwczovL2FwaS5jbWlsZS5jb20vdG9rZW4vIiwiaWF0IjoxNzI5ODMzMTA1LCJleHAiOjE3Mjk4MzY3MDV9.DuYyehXmCMpsuXMkeza7GX5twSKsCqWrZB1OhGkyfXc7Q2pFk3DBwLC6JaskhO15MQJsSM2b9UWRe1Lqy7az8DS7nWs-oQov-FOWNmsOu0aqGeG1rrRjp1erY4Y50qNoFzsm6VUCfpeoHyImvCiydL21wkou8vyXSrMWWirwekWSBEo99r-Q-dU3gDxDXP7IJTu-lZSKuWPKwzTpOV08CePASe8j1GmtzpEOGt2mOw8w7DgIBZCu2LqWjPbrVEEcqCyUbyQnHj8VQMjj3e5WFyMI8Zf40HDUUmL8x3E_5L-SdVNtYLnKXVR233erc36L85bRUCg2lS3WsqDsCfNWkA";
    }
}
