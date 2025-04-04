package com.ecommerce.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
  @Bean
  MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

  @Bean
  @ConditionalOnBean(MeterRegistry.class)
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
