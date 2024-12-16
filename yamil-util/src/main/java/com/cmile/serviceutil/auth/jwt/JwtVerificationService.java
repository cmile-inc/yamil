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

import com.auth0.jwt.JWT;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to verify a JWT token
 *
 * @author nishant-pentapalli
 */
@Service
public class JwtVerificationService {

  private final TokenVerifierFactory tokenVerifierFactory;

  @Autowired
  public JwtVerificationService(TokenVerifierFactory tokenVerifierFactory) {
    this.tokenVerifierFactory = tokenVerifierFactory;
  }

  public TokenVerificationResult verifyToken(String token) throws Exception {
    try {
      String issuer = getIssuerFromToken(token);
      ClientScope clientType = ClientScope.fromIssuer(issuer);
      TokenVerifier verifier = tokenVerifierFactory.getVerifier(clientType);
      if (verifier == null) {
        throw new Exception("No verifier found for client type: " + clientType);
      }
      return verifier.verifyToken(token);
    } catch (Exception e) {
      return TokenVerificationResult.INVALID;
    }
  }

  private String getIssuerFromToken(String token) {
    try {
      // Extract the issuer from the claims
      return JWT.decode(token).getIssuer();
    } catch (JwtException | IllegalArgumentException e) {
      // Handle token parsing exceptions and invalid tokens
      throw new RuntimeException("Failed to parse token or token is invalid", e);
    }
  }
}
