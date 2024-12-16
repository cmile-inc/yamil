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

package com.cmile.serviceutil.mongo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bson.Document;

@UtilityClass
public class TestSchemaCollectionMetadata {

  public Document buildValidationRules() {
    Document validationRules =
        new Document()
            .append("bsonType", "object")
            .append("required", List.of("name"))
            .append(
                "properties",
                new Document()
                    .append(
                        "name",
                        new Document("bsonType", "string")
                            .append("description", "must be a string and is required"))
                    .append(
                        "description",
                        new Document("bsonType", "string")
                            .append("description", "optional field that must be a string"))
                    .append(
                        "baseType",
                        new Document("bsonType", "string")
                            .append("description", "optional field that must be a string"))
                    .append(
                        "baseSchema",
                        new Document("bsonType", "object")
                            .append("description", "optional field that must be an object"))
                    .append(
                        "customSchema",
                        new Document("bsonType", "object")
                            .append("description", "optional field that must be an object")));
    return validationRules;
  }

  public List<Document> indexes() {
    Document nameUniqueIdx =
        new Document().append("field", "name").append("direction", "ASC").append("unique", true);
    return Collections.singletonList(nameUniqueIdx);
  }

  public List<Map<String, Object>> compoundIndexes() {
    Map<String, Object> compoundIndex1 = new HashMap<>();
    compoundIndex1.put(
        "fields", new Document("name", "ASC").append("baseType", "ASC").append("_id", "ASC"));
    return List.of(compoundIndex1);
  }
}
