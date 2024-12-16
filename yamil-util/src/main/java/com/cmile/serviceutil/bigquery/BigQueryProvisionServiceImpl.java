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

package com.cmile.serviceutil.bigquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BigQueryProvisionServiceImpl implements BigQueryProvisionService {

    private static final Logger logger = LoggerFactory.getLogger(BigQueryProvisionServiceImpl.class);

    @Autowired
    private BigQuery bigQuery;

    @Override
    public void createDataset(String datasetName) throws BigQueryException {
        try {
            // Check if the dataset exists
            Dataset dataset = getDataset(datasetName);
            if (null == dataset) {
                // Create a new BigQuery dataset
                DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();
                dataset = bigQuery.create(datasetInfo);
                logger.debug("Dataset " + dataset.getDatasetId().getDataset() + " created.");
            } else {
                logger.debug("Dataset " + dataset.getDatasetId().getDataset() + " already exists.");
            }
        } catch (BigQueryException e) {
            logger.error("Failed to access / create big query dataset: ", e);
            throw e;
        }
    }

    @Override
    public void deleteDataset(String datasetName) throws BigQueryException {
        try {
            if (null != getDataset(datasetName)) {
                bigQuery.delete(datasetName, BigQuery.DatasetDeleteOption.deleteContents());
                logger.debug("Dataset " + datasetName + " deleted.");
            }
        } catch (BigQueryException e) {
            logger.error("Failed to delete dataset: " + datasetName, e);
            throw e;
        }
    }

    @Override
    public void createContainerTable(String datasetName) throws Exception {
        String tableName = "container";
        try {

            String query = "CREATE OR REPLACE TABLE `" + datasetName
                    + ".container` ( `seller_org_id` STRING, `shipment_no` STRING, `container_id` STRING, `tracking_no` STRING, `origin_location_id` STRING, `ship_to_postal_code` STRING, `order_no` STRING, `warehouse_notification_ts` TIMESTAMP, `scac_code` STRING, `carrier_service` STRING, `expected_ship_date` TIMESTAMP, `expected_delivery_date` TIMESTAMP, `actual_ship_date` TIMESTAMP, `actual_delivery_date` TIMESTAMP, `shipment_insured_value` NUMERIC, `insurance_currency_code` STRING, `fulfillment_type` STRING, `is_last_mile` BOOLEAN, `container_weight` NUMERIC, `weight_uom` STRING, `container_length` NUMERIC, `length_uom` STRING, `container_width` NUMERIC, `width_uom` STRING, `container_height` NUMERIC, `height_uom` STRING, `container_volume` NUMERIC, `volume_uom` STRING, `carton_type` STRING, `shipping_cost` NUMERIC, `cost_currency_code` STRING, `quantity` NUMERIC, `zone` STRING);";
            // Check if the table exists
            Dataset dataset = getDataset(datasetName);
            if (null != dataset) {
                Table table = getTable(datasetName, tableName);
                if (null == table) {
                    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
                    bigQuery.query(queryConfig);
                    logger.debug("Table " + tableName + " created in dataset " + datasetName);
                } else {
                    logger.debug("Table " + tableName + " already exists in dataset " + datasetName);
                }
            } else {
                logger.error("Dataset " + datasetName + " does not exist.");
            }
        } catch (Exception e) {
            logger.error("Failed to create table: " + tableName, e);
            throw e;
        }
    }

    @Override
    public void deleteContainerTable(String datasetName) throws Exception {
        String tableName = "container";
        try {
            // Check if the table exists
            Dataset dataset = getDataset(datasetName);
            if (null != dataset) {
                Table table = getTable(datasetName, tableName);
                if (null != table) {
                    String query = "DROP TABLE `" + datasetName + "." + tableName + "`";
                    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
                    bigQuery.query(queryConfig);
                    logger.debug("Table " + tableName + " deleted from dataset " + datasetName);
                } else {
                    logger.debug("Table " + tableName + " does not exist in dataset " + datasetName);
                }
            } else {
                logger.error("Dataset " + datasetName + " does not exist.");
            }
        } catch (BigQueryException e) {
            logger.error("Failed to delete table: " + tableName, e);
            throw e;
        }
    }

    public Dataset getDataset(String tenantId) {
        return bigQuery.getDataset(tenantId);
    }

    public Table getTable(String dataset, String tableId) {
        return bigQuery.getTable(TableId.of(dataset, tableId));
    }

}
