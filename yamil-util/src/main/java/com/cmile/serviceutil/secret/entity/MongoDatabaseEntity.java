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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;

@JsonDeserialize(builder = MongoDatabaseEntity.Builder.class)
public class MongoDatabaseEntity {

  private final String username;
  private final String password;
  private final String domain;
  private final String database;
  private final String appName;
  private final String protocol;

  private MongoDatabaseEntity(Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
    this.domain = builder.domain;
    this.database = builder.database;
    this.appName = builder.appName;
    this.protocol = builder.protocol;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean isEmpty() {
    return username == null || password == null || domain == null || appName == null;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDomain() {
    return domain;
  }

  public String getDatabase() {
    return database;
  }

  public String getAppName() {
    return appName;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getConnectionString() {
    return String.format(
        "%s://%s:%s@%s/?appName=%s",
        protocol != null ? protocol : "mongodb+srv",
        URLEncoder.encode(username, StandardCharsets.UTF_8),
        URLEncoder.encode(password, StandardCharsets.UTF_8),
        domain,
        appName);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Builder {

    @Autowired ObjectMapper objectMapper;

    private String username;
    private String password;
    private String domain;
    private String database;
    private String appName;
    private String protocol;

    public Builder() {}

    @JsonIgnore
    public Builder fromFile(String filePath) {
      try {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MongoDatabaseEntity config =
            objectMapper.readValue(new File(filePath), MongoDatabaseEntity.class);
        this.username = config.username;
        this.password = config.password;
        this.domain = config.domain;
        this.database = config.database;
        this.appName = config.appName;
        this.protocol = config.protocol;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    @JsonIgnore
    public Builder fromEntity(MongoDatabaseEntity entity) {
      this.username = entity.username;
      this.password = entity.password;
      this.domain = entity.domain;
      this.database = entity.database;
      this.appName = entity.appName;
      this.protocol = entity.protocol;
      return this;
    }

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

    @JsonProperty("domain")
    public Builder domain(String domain) {
      this.domain = domain;
      return this;
    }

    @JsonProperty("database")
    public Builder database(String database) {
      this.database = database;
      return this;
    }

    @JsonProperty("appName")
    public Builder appName(String appName) {
      this.appName = appName;
      return this;
    }

    @JsonProperty("protocol")
    public Builder protocol(String protocol) {
      this.protocol = protocol;
      return this;
    }

    public MongoDatabaseEntity build() {
      return new MongoDatabaseEntity(this);
    }
  }
}
