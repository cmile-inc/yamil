# yamil-test

The `yamil-test` package provides a set of utilities and configurations for constructing test cases against various services such as MongoDB, PostgreSQL, GCP Storage, GCP Secret Manager, and more. These utilities are designed to facilitate the setup and execution of tests in a consistent and efficient manner.

## Project Structure

### Configuration Classes

The configuration classes in this package help set up the necessary environment for testing different services.

- **CfgPostgresTest**: Configuration class for setting up PostgreSQL tests.
  - [CfgPostgresTest.java](yamil/yamil-test/src/main/java/com/cmile/testutil/CfgPostgresTest.java)

- **CfgMongoTest**: Configuration class for setting up MongoDB tests.
  - [CfgMongoTest.java](yamil/yamil-test/src/main/java/com/cmile/testutil/CfgMongoTest.java)

- **CfgSecretTest**: Configuration class for setting up GCP Secret Manager tests.
  - [CfgSecretTest.java](yamil/yamil-test/src/main/java/com/cmile/testutil/CfgSecretTest.java)

- **CfgPubSubTest**: Configuration class for setting up Pub/Sub tests.
  - [CfgPubSubTest.java](yamil/yamil-test/src/main/java/com/cmile/testutil/CfgPubSubTest.java)

### Test Classes

The test classes in this package provide example test cases and utilities for testing various services.

- **GcpCloudStorageServiceTest**: Test class for `GcpCloudStorageService`.
  - [GcpCloudStorageServiceTest.java](yamil/yamil-test/src/test/java/com/cmile/serviceutil/storage/GcpCloudStorageServiceTest.java)

## Usage

To use the test utilities, you need to include the `yamil-test` package in your test dependencies and configure the necessary test classes.

### PostgreSQL

To set up PostgreSQL tests, use the `CfgPostgresTest` configuration class.

### MongoDB
To set up MongoDB tests, use the CfgMongoTest configuration class.

### GCP Secret Manager
To set up GCP Secret Manager tests, use the CfgSecretTest configuration class.

### GCP Storage
To set up GCP Storage tests, use the GcpCloudStorageServiceTest class as a reference.

## Contributing
We welcome contributions to this project. Please follow the guidelines below to contribute:

Fork the repository.
Create a new branch for your feature or bugfix.
Write your code and tests.
Ensure all tests pass.
Submit a pull request.
License
This project is licensed under the Apache License, Version 2.0. See the LICENSE file for details.