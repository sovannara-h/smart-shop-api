package com.ecommerce.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Slf4j
public class ConnectionMonitoringAspect {

  @Around("@annotation(transactional)")
  public Object monitorConnection(ProceedingJoinPoint joinPoint, Transactional transactional)
      throws Throwable {
    long startTime = System.currentTimeMillis();
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();

    try {
      log.debug("Starting transaction for {}.{}", className, methodName);
      Object result = joinPoint.proceed();
      long executionTime = System.currentTimeMillis() - startTime;

      if (executionTime > 1000) { // Log warning for slow transactions
        log.warn("Slow transaction detected in {}.{}: {}ms", className, methodName, executionTime);
      }

      log.debug("Transaction completed for {}.{} in {}ms", className, methodName, executionTime);
      return result;
    } catch (Exception e) {
      log.error("Transaction failed in {}.{}: {}", className, methodName, e.getMessage());
      throw e;
    }
  }
}
