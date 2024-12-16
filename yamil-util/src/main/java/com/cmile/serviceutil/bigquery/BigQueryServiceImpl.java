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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class BigQueryServiceImpl implements BigQueryService {

    private static final Logger logger = LoggerFactory.getLogger(BigQueryServiceImpl.class);

    @Autowired
    private BigQuery bigQuery;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public BigQueryImportDataStatusEnum importData(String datasetName, String tableName, String sourceUri)
            throws Exception {
        try {
            TableId tableId = TableId.of(datasetName, tableName);

            LoadJobConfiguration loadConfig = LoadJobConfiguration.newBuilder(tableId, sourceUri)
                    .setFormatOptions(FormatOptions.csv())
                    .setSchema(bigQuery.getTable(tableId).getDefinition().getSchema())
                    .setMaxBadRecords(10)
                    .setWriteDisposition(JobInfo.WriteDisposition.WRITE_TRUNCATE)
                    .build();

            Job createJob = bigQuery.create(JobInfo.of(loadConfig));

            Job completedJob = createJob.waitFor();
            if (completedJob.isDone() && completedJob.getStatus().getError() == null) {
                return BigQueryImportDataStatusEnum.SUCCESS;
            } else {
                return BigQueryImportDataStatusEnum.FAILED;
            }

        } catch (Exception e) {
            return BigQueryImportDataStatusEnum.RETRY;
        }
    }

    public TableResult executeQuery(String queryString) throws JobException, InterruptedException {
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryString).build();
        TableResult result = bigQuery.query(queryConfig);
        return result;
    }

    @Override
    public List<FieldValueList> query(String queryString) {
        TableResult result;
        try {
            result = executeQuery(queryString);
        } catch (InterruptedException e) {
            throw new RuntimeException("BigQuery query was interrupted", e);
        }

        List<FieldValueList> rows = new ArrayList<>();
        result.iterateAll().forEach(rows::add);

        return rows;
    }

    @Override
    public <T> List<T> query(String queryString, Class<T> clazz) {
        TableResult result;
        try {
            result = executeQuery(queryString);
        } catch (InterruptedException e) {
            throw new RuntimeException("BigQuery query was interrupted", e);
        }

        List<T> mappedResults = new ArrayList<>();
        Schema schema = result.getSchema();
        for (FieldValueList row : result.iterateAll()) {
            try {
                Map<String, Object> rowMap = new HashMap<>();
                schema.getFields().forEach(field -> {
                    String fieldName = field.getName();
                    if (row.get(fieldName) != null && !row.get(fieldName).isNull()) {
                        rowMap.put(fieldName, row.get(fieldName).getValue());
                    }
                });
                T instance = objectMapper.convertValue(rowMap, clazz);
                mappedResults.add(instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to map query result to object", e);
            }
        }

        return mappedResults;
    }
}
