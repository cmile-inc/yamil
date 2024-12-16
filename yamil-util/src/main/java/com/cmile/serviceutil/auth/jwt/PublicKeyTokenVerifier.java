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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;

/**
 * @author nishant-pentapalli
 */
public abstract class PublicKeyTokenVerifier implements TokenVerifier, Locator<Key> {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyTokenVerifier.class);

  private final PublicKey publicKey;

  PublicKeyTokenVerifier(String publicKeyStr) {
    String publicKeyContent =
        publicKeyStr
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
    byte[] publicKeyDecoded = Base64.getDecoder().decode(publicKeyContent);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyDecoded));
    } catch (Exception e) {
      log.error("Error while decoding public key", e);
      throw new RuntimeException("Error while decoding public key");
    }
  }

  @Override
  public TokenVerificationResult verifyToken(String token) throws JwtException {
    try {
      Jws<Claims> jwsClaims = Jwts.parser().keyLocator(this).build().parseSignedClaims(token);
      // parseClaimsJws will check expiration date. No need do here.

      return new TokenVerificationResult(true, jwsClaims.getPayload());
    } catch (JwtException | IllegalArgumentException e) {
      return TokenVerificationResult.INVALID;
    }
  }

  @Override
  public Key locate(Header header) {
    return this.publicKey;
  }
}
