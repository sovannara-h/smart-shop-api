package com.ecommerce.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

@Configuration
@Profile({"test", "dev", "prod"})
public class ResilienceConfig {

    @Value("${app.resilience.rate-limit:1000}")
    private int rateLimit;

    @Value("${app.resilience.concurrent-calls:50}")
    private int concurrentCalls;

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .permittedNumberOfCallsInHalfOpenState(2)
            .slidingWindowSize(2)
            .build();
            
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean
    public RateLimiterConfig createProductRateLimiterConfig() {
        return RateLimiterConfig.custom()
            .limitForPeriod(rateLimit)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(5))
            .build();
    }
    
    @Bean
    public BulkheadConfig createProductBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(concurrentCalls)
            .maxWaitDuration(Duration.ofMillis(500))
            .build();
    }
}