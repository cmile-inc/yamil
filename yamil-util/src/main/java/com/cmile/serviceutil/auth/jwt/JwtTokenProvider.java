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

import com.cmile.serviceutil.secret.SecretManagerService;
import com.cmile.serviceutil.secret.SecretTypeEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements Locator<Key> {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(JwtTokenProvider.class);

  private static final String ISSUER = "https://api.cmile.com/service/";
  private static final String SCOPE_CLAIM = "scope";
  private static final String CLIENT_ID_CLAIM = "cid";
  private static final String CALLER_SERVICE_ID_CLAIM = "callerServiceId";
  private static final String SPACE_ID_CLAIM = "sid";
  private static final String TENANT_ID_CLAIM = "tid";
  private static final String DU_ID_CLAIM = "duId";

  private PublicKey secretKey;

  @Value("${jwt.expiration:3600000}")
  private long validityInMilliseconds;

  @Autowired private SecretManagerService secretManager;

  private KeyPair keyPair;

  @PostConstruct
  public void init() throws Exception {
    this.keyPair =
        loadKeyPair(
            secretManager.getSecret(SecretTypeEnum.INTERNAL_PRIVATE_KEY, null),
            secretManager.getSecret(SecretTypeEnum.INTERNAL_PUBLIC_KEY, null));

    this.secretKey =
        getSecretKey(secretManager.getSecret(SecretTypeEnum.CUSTOMER_PUBLIC_KEY, null));
  }

  private KeyPair loadKeyPair(String privateKeyData, String publicKeyData) throws Exception {
    String privateKeyContent =
        privateKeyData
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

    String publicKeyContent =
        publicKeyData
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

    byte[] privateKeyDecoded = Base64.getDecoder().decode(privateKeyContent);
    byte[] publicKeyDecoded = Base64.getDecoder().decode(publicKeyContent);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyDecoded));
    PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyDecoded));

    return new KeyPair(publicKey, privateKey);
  }

  public String createToken(
      ClientScope scope,
      String callerServiceId,
      String clientId,
      String spaceId,
      String tenantId,
      String duId) {

    var claimsBuilder = Jwts.claims().subject(callerServiceId);
    claimsBuilder.issuer(ISSUER);
    claimsBuilder.add(SCOPE_CLAIM, ClientScope.SERVICE.name());
    claimsBuilder.add(CLIENT_ID_CLAIM, clientId);
    claimsBuilder.add(CALLER_SERVICE_ID_CLAIM, callerServiceId);
    if (spaceId != null) {
      claimsBuilder.add(SPACE_ID_CLAIM, spaceId);
    }
    if (tenantId != null) {
      claimsBuilder.add(TENANT_ID_CLAIM, tenantId);
    }
    if (duId != null) {
      claimsBuilder.add(DU_ID_CLAIM, duId);
    }

    var claims = claimsBuilder.build();
    Date now = new Date();
    long validityInMilliseconds = this.validityInMilliseconds;
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .header()
        .add(SCOPE_CLAIM, scope)
        .and()
        .claims(claims)
        .issuedAt(now)
        .expiration(validity)
        .signWith(this.keyPair.getPrivate(), Jwts.SIG.RS256)
        .compact();
  }

  public Claims validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().keyLocator(this).build().parseSignedClaims(token);
      // parseClaimsJws will check expiration date. No need to do here.

      return claims.getPayload();
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtException("Invalid JWT token: " + e.getMessage());
    }
  }

  public PublicKey getSecretKey(String publicKey) throws RuntimeException {
    String publicKeyContent =
        publicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

    byte[] publicKeyDecoded = Base64.getDecoder().decode(publicKeyContent);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyDecoded));
    } catch (Exception e) {
      log.error("Error while decoding public key", e);
      throw new RuntimeException("Error while decoding public key");
    }
  }

  public boolean validateUserToken(Claims claims, String spaceId, String tenantId) {
    try {
      if (claims.get(SPACE_ID_CLAIM) == null && claims.get(TENANT_ID_CLAIM) == null) {
        return true;
      } else if (spaceId != null && claims.get(SPACE_ID_CLAIM).equals(spaceId)) {
        return true;
      } else if (tenantId != null && claims.get(TENANT_ID_CLAIM).equals(tenantId)) {
        return true;
      }
      // parseClaimsJws will check expiration date. No need to do here.
      log.debug("expiration date: {}", claims.getExpiration());
      return false;
    } catch (RuntimeException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    }
    return false;
  }

  public static boolean isInternalToken(Header header) {
    if (header.get(SCOPE_CLAIM) == null) {
      return false;
    }
    return ClientScope.SERVICE.name().equalsIgnoreCase(header.get(SCOPE_CLAIM).toString());
  }

  public static boolean isInternalToken(Claims claim) {
    if (claim.get(SCOPE_CLAIM) == null) {
      return false;
    }
    return ClientScope.SERVICE.name().equalsIgnoreCase(claim.get(SCOPE_CLAIM).toString());
  }

  @Override
  public Key locate(Header header) {
    return isInternalToken(header) ? this.keyPair.getPublic() : this.secretKey;
  }
}
