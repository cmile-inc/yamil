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
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author nishant-pentapalli
 */
@Component
public class SpaceClientTokenVerifier extends PublicKeyTokenVerifier {

  @Autowired
  public SpaceClientTokenVerifier(SecretManagerService secretManagerService) {
    super(secretManagerService.getSecret(SecretTypeEnum.CUSTOMER_PUBLIC_KEY, null));
  }

  @Override
  public TokenVerificationResult verifyToken(String token) throws JwtException {
    // @TODO: Implement any space specific token verification logic here
    return super.verifyToken(token);
  }
}
