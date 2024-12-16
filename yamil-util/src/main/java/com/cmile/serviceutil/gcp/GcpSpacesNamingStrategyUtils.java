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

package com.cmile.serviceutil.gcp;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GcpSpacesNamingStrategyUtils {

  // Static final variable for the bucket name prefix
  private static final String BUCKET_NAME_PREFIX = "cmile-space-";

  public String bucketName(String spaceId) {
    return BUCKET_NAME_PREFIX + spaceId.toLowerCase();
  }
}
