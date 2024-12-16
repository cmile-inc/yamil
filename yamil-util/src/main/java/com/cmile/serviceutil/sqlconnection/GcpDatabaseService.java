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

package com.cmile.serviceutil.sqlconnection;

import com.cmile.serviceutil.gcp.GCPServiceProject;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class GcpDatabaseService {

  private static final String DU_INSTANCE_NAME_SUFFIX = "-postgres-db-instance";
  private final GoogleCredentials credentials;
  private final String serviceName;
  private final String projectId;

  public GcpDatabaseService(GCPServiceProject gcpServiceProject) {
    this.credentials = gcpServiceProject.getGoogleCredentials();
    this.serviceName = gcpServiceProject.getApplicationName();
    this.projectId = gcpServiceProject.getProjectId();
  }

  public static String getInstanceIdForDeploymentUnitId(String deploymentUnitId) {
    return deploymentUnitId + DU_INSTANCE_NAME_SUFFIX;
  }

  public List<Database> getDatabaseList(String instanceId) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();

    // List databases in the instance
    SQLAdmin.Databases.List request = sqlAdminService.databases().list(projectId, instanceId);
    DatabasesListResponse response = request.execute();
    return response.getItems();
  }

  public String getDatabaseHost(String instanceId) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();

    // Get instance details
    SQLAdmin.Instances.Get request = sqlAdminService.instances().get(projectId, instanceId);
    DatabaseInstance instance = request.execute();

    // Get the first IP address (assuming it's the primary IP)
    List<IpMapping> ipAddresses = Optional.ofNullable(instance.getIpAddresses())
        .orElseThrow(
            () -> new IOException("No IP addresses found for instance: " + instanceId));
    return ipAddresses.get(0).getIpAddress();
  }

  public void createDatabase(String instanceId, String databaseName) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();

    Database database = new Database();
    database.setName(databaseName);
    SQLAdmin.Databases.Insert request = sqlAdminService.databases().insert(projectId, instanceId, database);
    request.execute();
  }

  public void dropDatabase(String instanceId, String databaseName) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();

    SQLAdmin.Databases.Delete request = sqlAdminService.databases().delete(projectId, instanceId, databaseName);
    request.execute();
  }

  public List<User> getDBUserList(String instanceId) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();

    // List databases in the instance
    SQLAdmin.Users.List request = sqlAdminService.users().list(projectId, instanceId);
    UsersListResponse response = request.execute();
    return response.getItems();
  }

  public void createDBUser(String instanceId, String userName, String password) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();

    User user = new User();
    user.setHost("%");
    user.setName(userName);
    user.setPassword(password);
    SQLAdmin.Users.Insert request = sqlAdminService.users().insert(projectId, instanceId, user);
    request.execute();
  }

  public void deleteDBUser(String instanceId, String userName) throws IOException {
    SQLAdmin sqlAdminService = new SQLAdmin.Builder(
        new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
        .setApplicationName(serviceName)
        .build();
    SQLAdmin.Users.Delete request = sqlAdminService.users().delete(projectId, instanceId);
    request.setName(userName);
    request.execute();
  }
}
