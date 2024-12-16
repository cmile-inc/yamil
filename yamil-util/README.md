# com.cmile.common.service-util

## Overview
The com.cmile.common.service-util library provides utilities for working with various services and features. 
Each feature includes classes prefixed with Cfg for easy configuration. Below are instructions for setting 
up and using different utilities in your projects.

## Usage
To incorporate a feature, include its respective configuration class (e.g., CfgPostgres) in your main configuration class, 
then follow the setup instructions for that feature.

### BigQuery

To use the BigQuery utilities, you need to configure and instantiate the `BigQueryService` and `BigQueryProvisionService` classes.

- **BigQueryService**: Provides methods for querying and importing data into BigQuery.
  - [BigQueryService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/bigquery/BigQueryService.java)
  - [BigQueryServiceImpl.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/bigquery/BigQueryServiceImpl.java)

- **BigQueryProvisionService**: Provides methods for managing BigQuery datasets and tables.
  - [BigQueryProvisionService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/bigquery/BigQueryProvisionService.java)
  - [BigQueryProvisionServiceImpl.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/bigquery/BigQueryProvisionServiceImpl.java)

### MongoDB

To use the MongoDB utilities, you need to configure and instantiate the `DynamicMongoTemplate`, `MongoCacheManager`, and `AtlasMongoDbService` classes.

- **DynamicMongoTemplate**: Provides dynamic MongoDB template instances based on the current context.
  - [DynamicMongoTemplate.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/mongo/DynamicMongoTemplate.java)

- **MongoCacheManager**: Manages caching of MongoDB connection details.
  - [MongoCacheManager.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/mongo/MongoCacheManager.java)

- **AtlasMongoDbService**: Service for managing MongoDB collections and users in MongoDB Atlas.
  - [AtlasMongoDbService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/mongo/AtlasMongoDbService.java)

#### Example

```java

//This will register the required beans for mongo connection
import com.cmile.serviceutil.mongo.CfgMongo;

@Import({
    CfgMongo.class,
    CfgCommon.class
})
class AppConfig {

}


@Repository
public class ItemDataRepositoryImpl implements ItemDataRepository {

  private final DynamicMongoTemplate dynamicMongoTemplate;

  @Autowired
  public ItemDataRepositoryImpl(
      DynamicMongoTemplate dynamicMongoTemplate) {
    this.dynamicMongoTemplate = dynamicMongoTemplate;
  }

  @Override
  public ItemDocument saveDocument(Item doc) {
    dynamicMongoTemplate.getMongoTemplate().save(doc);
    return doc;
  }
}
```
  
### API Invoker

To use the API invoker utilities, you need to configure and instantiate the relevant classes for making API calls.

- **ApiInvokerService**: Service for invoking external APIs.
  - [ApiInvokerService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/apiinvoker/ApiInvokerService.java)

#### Example

```java

import com.cmile.serviceutil.apiinvoker.CfgApiInvoker;
import com.cmile.serviceutil.mongo.CfgMongo;

@Import({
        CfgApiInvoker.class,
        CfgCommon.class
})
class AppConfig {

}


//Webclient class
class ExternalApiClient {
  public Mono<?> getAllInfo(Integer from, Integer size) throws WebClientResponseException {
    ParameterizedTypeReference<?> localVarReturnType = new ParameterizedTypeReference<?>() {};
    return getAllInfo(from, size).bodyToMono(localVarReturnType);
  }

}

@Service
public class ExternalApiCallExample {

  private Result loadSpaceDetails(String id) {
    ExternalApiClient externalApiClient = new ExternalApiClient();
    Object result = apiInvoker.invoke(
            ExternalApiClient.getApiClient(),
            () -> externalApiClient.getAllInfo("id", id));
    
    return result;
  }
}
```

### Metrics

To use the metrics utilities, you need to configure and instantiate the relevant classes for collecting and reporting metrics.

- **MetricsService**: Service for collecting and reporting application metrics.
  - [MetricsService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/metrics/MetricsService.java)

### PostgreSQL

To use the PostgreSQL utilities, you need to configure and instantiate the `PostgresCacheManager` and `LiquibaseService` classes.

- **PostgresCacheManager**: Manages caching of PostgreSQL connection details.
  - [PostgresCacheManager.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/sqlconnection/PostgresCacheManager.java)

- **LiquibaseService**: Service for managing database migrations using Liquibase.
  - [LiquibaseService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/sqlconnection/migration/LiquibaseService.java)

#### Example

```java

// Including the CfgAuth to the main configuration class will automatically enable JWT Authentication


import com.cmile.serviceutil.common.CfgCommon;
import com.cmile.serviceutil.sqlconnection.CfgPostgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

@Import({
        CfgCommon.class,
        CfgPostgres.class
})
class AppConfig {

}

//Repository
interface TestRepository extends JpaRepository<Object, String> {

}

//DAO
class TestDAO {

  @Autowired
  TestRepository testRepository;
  
  public void save() {
    testRepository.save(new Object());
  }
}


```

### GCP Secret Manager

To use the GCP Secret Manager utilities, you need to configure and instantiate the `SecretManagerService` class.

- **SecretManagerService**: Service for managing secrets in GCP Secret Manager.
  - [SecretManagerService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/secret/SecretManagerService.java)

### GCP Storage

To use the GCP Storage utilities, you need to configure and instantiate the `GcpCloudStorageService` class.

- **GcpCloudStorageService**: Service for managing files in GCP Cloud Storage.
  - [GcpCloudStorageService.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/storage/GcpCloudStorageService.java)

#### Example

```java

import com.cmile.serviceutil.common.CfgCommon;
import com.cmile.serviceutil.storage.CfgStorage;

@Import({
        CfgCommon.class,
        CfgStorage.class
})
class AppConfig {

}

class TestGcpStorage {

  @Autowired
  private GcpCloudStorageService gcpCloudStorageService;

  public void saveFileToGcp() {
    gcpCloudStorageService.createBucket();
    // Similary have public methods to upload file etc. The class can be referred for the methods available
  }
}


```

### Logging

To use the logging utilities, you need to configure and instantiate the `CfgLogging` class.

- **CfgLogging**: Configuration class for setting up logging.
  - [CfgLogging.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/logging/CfgLogging.java)

### Auth

To use jwt authentication, you need to configure and instantiate `CfgAuth` class.

- **CfgAuth**: Configuration class for setting up logging.
  - [CfgAuth.java](yamil/yamil-util/src/main/java/com/cmile/serviceutil/auth/CfgAuth.java)

#### Example

```java

// Including the CfgAuth to the main configuration class will automatically enable JWT Authentication


import com.cmile.serviceutil.auth.CfgAuth;

@Import({
        CfgAuth.class,
        CfgCommon.class
})
class AppConfig {

}
```

## Contributing

We welcome contributions to this project. Please follow the guidelines below to contribute:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Write your code and tests.
4. Ensure all tests pass.
5. Submit a pull request.

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.