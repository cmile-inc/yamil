# Environment Setup

Add the following properties to your environment configuration by referring to the properties file
located in the **metricsetup** folder.

## In the service repository:

1. Navigate to the `AppConfig.class`.
2. Add the annotation `@Import({CfgMetricRegistry.class})`.

After completing these steps, restart the application to push the metrics to Google Cloud Platform (
GCP) under custom metrics.

## To view metrics in Metrics Explorer:

1. Select **Global** as the metric type, then navigate to **Custom Metrics** and
   choose `<metric name>`. The available metric names are:
    - `serviceName_api_error_count`
    - `serviceName_api_response_time`
    - `serviceName_api_response_size`
    - `serviceName_api_request_input_size`

## To view metrics in Grafana:

1. Create a service account with the **Monitoring Viewer** role.
2. Download the credentials JSON file.
3. Upload the JSON file in Grafana to import the data.
4. In Grafana, follow these steps:
    - Navigate to the **Connections** menu.
    - Select **Add new datasource** -> **Google Cloud Monitor**.
5. To view your custom dashboard, select:
    - The project specified in the credentials.
    - The service as **Custom**.
    - The metric name from the options below:
        - `serviceName_api_error_count`
        - `serviceName_api_response_time`
        - `serviceName_api_response_size`
        - `serviceName_api_request_input_size`
