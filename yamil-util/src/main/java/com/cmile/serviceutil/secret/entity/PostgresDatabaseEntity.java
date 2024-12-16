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

package com.cmile.serviceutil.secret.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = PostgresDatabaseEntity.Builder.class)
public class PostgresDatabaseEntity {

  private final String username;
  private final String password;
  private final String host;
  private final int port;
  private final String database;

  private PostgresDatabaseEntity(Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
    this.host = builder.host;
    this.port = builder.port;
    this.database = builder.database;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonIgnore
  public boolean isEmpty() {
    return username == null || password == null || host == null || database == null;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabase() {
    return database;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getConnectionString() {
    return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Builder {

    private String username;
    private String password;
    private String host;
    private int port;
    private String database;

    public Builder() {}

    @JsonIgnore
    public Builder fromEntity(PostgresDatabaseEntity entity) {
      this.username = entity.username;
      this.password = entity.password;
      this.host = entity.host;
      this.port = entity.port;
      this.database = entity.database;
      return this;
    }

    // Getters and Setters
    @JsonProperty("username")
    public Builder username(String username) {
      this.username = username;
      return this;
    }

    @JsonProperty("password")
    public Builder password(String password) {
      this.password = password;
      return this;
    }

    @JsonProperty("host")
    public Builder host(String host) {
      this.host = host;
      return this;
    }

    @JsonProperty("port")
    public Builder port(int port) {
      this.port = port;
      return this;
    }

    @JsonProperty("database")
    public Builder database(String database) {
      this.database = database;
      return this;
    }

    public PostgresDatabaseEntity build() {
      return new PostgresDatabaseEntity(this);
    }
  }
}
