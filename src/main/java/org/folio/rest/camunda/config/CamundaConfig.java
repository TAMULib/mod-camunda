package org.folio.rest.camunda.config;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class CamundaConfig {

  @Bean
  Clock clock() {

    return Clock.systemDefaultZone();
  }

  @Bean
  ConcurrentHashMap<String, String> concurrentFolioTokensRecordHashMap() {

    return new ConcurrentHashMap<>();
  }

  @Bean
  @Order(Ordering.DEFAULT_ORDER + 1)
  public static ProcessEnginePlugin processEnginePlugin() {
    return new CamundaEngineConfig();
  }

}
