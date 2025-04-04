package com.ecommerce.config;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Aspect
@Configuration
@Profile("!dev")
public class RateLimiterAspectConfig {

  @Around("@annotation(rateLimiter)")
  public Object rateLimiterAroundAdvice(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter)
      throws Throwable {
    return joinPoint.proceed();
  }
}
