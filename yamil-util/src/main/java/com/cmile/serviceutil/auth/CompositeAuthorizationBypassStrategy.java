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

package com.cmile.serviceutil.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CompositeAuthorizationBypassStrategy is a composite of multiple AuthorizationBypassStrategy.
 *
 * @author nishant-pentapalli
 */
@Component
public class CompositeAuthorizationBypassStrategy implements AuthorizationBypassStrategy {

  private final List<AuthorizationBypassStrategy> strategies;

  @Autowired
  public CompositeAuthorizationBypassStrategy(List<AuthorizationBypassStrategy> strategies) {
    this.strategies = strategies;
  }

  @Override
  public boolean shouldBypass(HttpServletRequest request) {
    return strategies.stream().anyMatch(strategy -> strategy.shouldBypass(request));
  }
}
