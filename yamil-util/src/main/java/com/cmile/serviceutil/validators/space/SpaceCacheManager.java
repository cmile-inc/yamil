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

package com.cmile.serviceutil.validators.space;

import com.cmile.serviceutil.apiinvoker.ApiInvoker;
import com.cmile.serviceutil.cache.CacheManager;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpaceCacheManager extends CacheManager<String, Object> {

  private final GCPServiceProject gcpServiceProject;
  private final ApiInvoker apiInvoker;

  private final ObjectMapper objectMapper;
  private final SpacePlatformService platformService;

  @Autowired
  public SpaceCacheManager(
      GCPServiceProject gcpServiceProject,
      ApiInvoker apiInvoker,
      ObjectMapper objectMapper, SpacePlatformService platformService) {
    super(5, TimeUnit.MINUTES, 500);
    this.gcpServiceProject = gcpServiceProject;
    this.apiInvoker = apiInvoker;
    this.objectMapper = objectMapper;
    this.platformService = platformService;
    this.setLoader(this::loadSpaceDetails);
  }

  private SpaceDetails loadSpaceDetails(String id) {
    log.debug("Loading space details for ID: {}", id);
    SpaceDetails result = platformService.getSpaceDetails(id);
    log.debug("Space details for ID: {} are loaded", id);
    return result;
  }
}
