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

package com.cmile.serviceutil.metric;

import com.cmile.serviceutil.gcp.CfgGCPProject;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.stackdriver.StackdriverConfig;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

@Configuration
@Import({CfgGCPProject.class})
@ConfigurationProperties(prefix = "meter.registry")
@ComponentScan(value = "com.cmile.serviceutil.metric")
public class CfgMetricRegistry {

  public static final String SERVICE_NAME_TAG = "serviceName";

  public static final String PROJECT_ID = "stackdriver.projectId";
  public static final String STEP = "stackdriver.step";
  public static final String CONNECT_TIMEOUT = "stackdriver.connectTimeout";
  public static final String READ_TIMEOUT = "stackdriver.readTimeout";
  public static final String BATCH_SIZE = "stackdriver.batchSize";
  public static final String NUM_THREADS = "stackdriver.numThreads";
  public static final String CREDENTIALS = "stackdriver.credentials";

  @Value("${stackdriver.step:1m}")
  private String step;

  @Value("${stackdriver.connectTimeout:5s}")
  private String connectTimeout;

  @Value("${stackdriver.readTimeout:5s}")
  private String readTimeout;

  @Value("${stackdriver.batchSize:10}")
  private int batchSize;

  @Value("${stackdriver.numThreads:2}")
  private int numThreads;

  @Autowired private GCPServiceProject gcpServiceProject;

  @Bean
  @Primary
  public MeterRegistry meterRegistry(@Autowired List<MeterRegistry> meterRegistries) {
    CompositeMeterRegistry compositeRegistry = new CompositeMeterRegistry();
    meterRegistries.forEach(compositeRegistry::add);
    return compositeRegistry;
  }

  @Bean
  @Qualifier("loggingMeterRegistry")
  @ConditionalOnProperty(
      name = "meter.registry.logging.enabled",
      havingValue = "true",
      matchIfMissing = false)
  public LoggingMeterRegistry loggingMeterRegistry() {
    return new LoggingMeterRegistry();
  }

  @Bean
  @Qualifier("stackdriverMeterRegistry")
  @ConditionalOnProperty(
      name = "meter.registry.stackdriver.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public StackdriverMeterRegistry stackdriverMeterRegistry() {
    StackdriverMeterRegistry registry =
        StackdriverMeterRegistry.builder(
                new StackdriverConfig() {
                  @Override
                  public String get(String key) {
                    switch (key) {
                      case PROJECT_ID:
                        return gcpServiceProject.getProjectId();
                      case STEP:
                        return step;
                      case CONNECT_TIMEOUT:
                        return connectTimeout;
                      case READ_TIMEOUT:
                        return readTimeout;
                      case BATCH_SIZE:
                        return String.valueOf(batchSize);
                      case NUM_THREADS:
                        return String.valueOf(numThreads);
                      case CREDENTIALS:
                        return gcpServiceProject.getGoogleServiceAccountFileLocation();
                      default:
                        return null;
                    }
                  }
                })
            .build();
    registry.config().commonTags(getCommonTags());
    return registry;
  }

  @Bean
  public FilterRegistrationBean<RestApiMetricsFilter> metricsFilterFilterRegistrationBean(
      RestApiMetricsFilter metricsFilter) {
    String suffix = "/*";
    if (StringUtils.isNotBlank(gcpServiceProject.getApplicationName())) {
      suffix = gcpServiceProject.getApplicationName().split("-")[1];
      suffix = String.format("/*", suffix.toLowerCase());
    }

    FilterRegistrationBean<RestApiMetricsFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(metricsFilter);
    registrationBean.addUrlPatterns(suffix);
    registrationBean.setOrder(5);
    return registrationBean;
  }

  /**
   * Method to build common tags dynamically
   *
   * @return List<Tag> list of common tags
   */
  private List<Tag> getCommonTags() {

    List<Tag> commonTags = new ArrayList<>();
    commonTags.add(Tag.of(SERVICE_NAME_TAG, gcpServiceProject.getApplicationName()));
    return commonTags;
  }
}
