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

package com.cmile.serviceutil.secret.helper;

import com.cmile.serviceutil.secret.entity.MongoDatabaseEntity;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoDatabaseConnection {

  public static MongoClient mongoClient(MongoDatabaseEntity config) {
    MongoClientSettings mongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(config.getConnectionString()))
            .build();

    return MongoClients.create(mongoClientSettings);
  }
}
