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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Extracts different types of context information from JWT claims. */
public class ContextExtractor {

  // Private static keys
  private static final String KEY_DU_ID = "duId";
  private static final String KEY_IDP_USER_ID = "idpUserId";
  private static final String KEY_CMILE_USER_ID = "cmileUserId";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_SPACE_ID = "sid";
  private static final String KEY_CALLER_SERVICE_ID = "callerServiceId";
  private static final String KEY_CID = "cid";
  private static final String KEY_TID = "tid";

  static ContextInfo getUserContext(Claims claims) {
    ContextInfo contextInfo = new ContextInfo();
    contextInfo.setSpaceId(
        Optional.ofNullable(claims.get(KEY_SPACE_ID)).map(Object::toString).orElse(null));
    contextInfo.setDuId(
        Optional.ofNullable(claims.get(KEY_DU_ID)).map(Object::toString).orElse(null));
    contextInfo.setClientType(ClientScope.USER.name());
    contextInfo.setAdditionalInfo(
        Map.of(
            KEY_IDP_USER_ID,
            Optional.ofNullable(claims.get(KEY_IDP_USER_ID)).map(Object::toString).orElse(null),
            KEY_CMILE_USER_ID,
            Optional.ofNullable(claims.get(KEY_CMILE_USER_ID)).map(Object::toString).orElse(null),
            KEY_EMAIL,
            Optional.ofNullable(claims.get(KEY_EMAIL)).map(Object::toString).orElse(null)));
    return contextInfo;
  }

  static ContextInfo getSpaceContext(Claims claims) {
    ContextInfo contextInfo = new ContextInfo();
    contextInfo.setSpaceId(
        Optional.ofNullable(claims.get(KEY_SPACE_ID)).map(Object::toString).orElse(null));
    contextInfo.setDuId(
        Optional.ofNullable(claims.get(KEY_DU_ID)).map(Object::toString).orElse(null));
    contextInfo.setClientType(ClientScope.SPACE.name());
    return contextInfo;
  }

  static ContextInfo getServiceContext(Claims claims) {
    ContextInfo contextInfo = new ContextInfo();
    contextInfo.setSpaceId(
        Optional.ofNullable(claims.get(KEY_SPACE_ID)).map(Object::toString).orElse(null));
    contextInfo.setDuId(
        Optional.ofNullable(claims.get(KEY_DU_ID)).map(Object::toString).orElse(null));
    contextInfo.setClientType(ClientScope.SPACE.name());

    String callerServiceId =
        Optional.ofNullable(claims.get(KEY_CALLER_SERVICE_ID)).map(Object::toString).orElse(null);
    String cid = Optional.ofNullable(claims.get(KEY_CID)).map(Object::toString).orElse(null);
    String tid = Optional.ofNullable(claims.get(KEY_TID)).map(Object::toString).orElse(null);

    Map<String, String> addVals = new HashMap<>();
    addVals.put(KEY_CALLER_SERVICE_ID, callerServiceId);
    addVals.put(KEY_CID, cid);
    addVals.put(KEY_TID, tid);
    contextInfo.setAdditionalInfo(addVals);
    return contextInfo;
  }
}
