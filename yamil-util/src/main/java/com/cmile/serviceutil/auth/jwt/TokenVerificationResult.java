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

/**
 * The result of token verification
 *
 * @author nishant-pentapalli
 */
public class TokenVerificationResult {

  public static final TokenVerificationResult INVALID = new TokenVerificationResult(false, null);

  private final boolean isValid;
  private final Claims claims;

  public TokenVerificationResult(boolean isValid, Claims claims) {
    this.isValid = isValid;
    this.claims = claims;
  }

  public boolean isValid() {
    return isValid;
  }

  public Claims getClaims() {
    return claims;
  }
}
