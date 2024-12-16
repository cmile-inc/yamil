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

package com.cmile.serviceutil.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author nishant-pentapalli
 */
@Component
public class JsonEntityMapper {

  Logger logger = LoggerFactory.getLogger(JsonEntityMapper.class);
  private final ObjectMapper objectMapper;

  public JsonEntityMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T readJsonToEntity(String json, Class<T> entityClass) throws IOException {
    try {
      return objectMapper.readValue(json, entityClass);
    } catch (Exception e) {
      logger.error("Error while deserializing JSON to entity", e);
      throw e;
    }
  }

  // Method to write an entity to JSON
  public <T> String writeEntityToJson(T entity) throws IOException {
    try {
      return objectMapper.writeValueAsString(entity);
    } catch (IOException e) {
      logger.error("Error while serializing entity to JSON", e);
      throw e;
    }
  }
}
