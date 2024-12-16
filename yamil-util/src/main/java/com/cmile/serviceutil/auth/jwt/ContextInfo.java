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

import java.util.Map;
import java.util.HashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ContextInfo {

  @Setter
  String applicationName;

  @Setter
  String spaceId;

  @Setter
  String duId;

  @Setter
  String correlationId;

  @Setter(AccessLevel.PACKAGE)
  String clientType;

  private Map<String, String> additionalInfo;

  public Map<String, String> getAdditionalInfo() {
    if (additionalInfo == null) {
      // Invariably this is needed due to metrics & logging, and therefore its better
      // that this is
      // initialized here
      additionalInfo = new HashMap<>();
    }
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, String> additionalInfo) {
    if (additionalInfo != null) {
      getAdditionalInfo().clear();
      getAdditionalInfo().putAll(additionalInfo);
    } else {
      // Just clear the additional info map
      // But do not make it null. Basically our additional info map should never be
      // null
      getAdditionalInfo().clear();
    }
  }
}
