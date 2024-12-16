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

package com.cmile.serviceutil.secret;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {

  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!^*()-_+=<>";

  public String generateRandomPassword(int length) {
    SecureRandom random = new SecureRandom();
    StringBuilder password = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(CHARACTERS.length());
      password.append(CHARACTERS.charAt(index));
    }

    return password.toString();
  }
}
