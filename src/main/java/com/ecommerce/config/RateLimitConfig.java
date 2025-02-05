package com.ecommerce.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;

@Configuration
@Profile("!dev")
public class RateLimitConfig {
    @Bean
    public RateLimiterConfig productRateLimiterConfig() {
        return RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(1))
            .build();
    }
} 