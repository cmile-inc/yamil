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

package com.cmile.serviceutil.util;

import com.cmile.serviceutil.mongo.MongoUtilityColumn;
import com.cmile.serviceutil.sqlconnection.PostgresUtilityColumns;
import java.time.LocalDateTime;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;

public class UtilityColumnsAuditableAspect {

  @Before("@annotation(utilityColumnsAuditable)")
  public void setAuditFieldsBefore(
      JoinPoint joinPoint, UtilityColumnsAuditable utilityColumnsAuditable) {
    // Modify the arguments before method execution
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {
      LocalDateTime now = LocalDateTime.now();
      if (args[i] instanceof PostgresUtilityColumns doc) {
        if (doc.getCreatedTs() == null) {
          doc.setCreatedTs(now);
        }
        doc.setUpdatedTs(now);

        if (doc.getCreatedBy() == null) {
          doc.setCreatedBy("TODO from JWT"); // TODO Replace with actual user retrieval logic
        }
        doc.setUpdatedBy("TODO from JWT"); // TODO Replace with actual user retrieval logic

        args[i] = doc; // Update the argument with the modified object
      } else if (args[i] instanceof MongoUtilityColumn doc) {
        if (doc.getCreatedTs() == null) {
          doc.setCreatedTs(now);
        }
        doc.setUpdatedTs(now);

        if (doc.getCreatedBy() == null) {
          doc.setCreatedBy("TODO from JWT"); // Replace with actual user retrieval logic
        }
        doc.setUpdatedBy("TODO from JWT"); // Replace with actual user retrieval logic

        args[i] = doc; // Update the argument with the modified object
      }
    }
  }
}
