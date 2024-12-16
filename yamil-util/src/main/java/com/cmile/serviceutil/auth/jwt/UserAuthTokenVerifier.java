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

package com.cmile.serviceutil.auth.jwt;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Verifies the user token
 *
 * @author nishant-pentapalli
 */
@Component
public class UserAuthTokenVerifier implements TokenVerifier {

  private final FirebaseAuth firebaseAuth;

  @Autowired
  public UserAuthTokenVerifier(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  @Override
  public TokenVerificationResult verifyToken(String token) {
    try {
      FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

      return new TokenVerificationResult(true, new DefaultClaims(decodedToken.getClaims()));
    } catch (Exception e) {
      return TokenVerificationResult.INVALID;
    }
  }
}
