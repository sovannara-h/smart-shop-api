package com.ecommerce.audit;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class SecretAccessAudit {
  @Around("execution(* com.ecommerce.config.VaultConfig.*(..))")
  public Object auditSecretAccess(ProceedingJoinPoint joinPoint) throws Throwable {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String user = auth != null ? auth.getName() : "SYSTEM";

    log.info("Secret access by {} at {}", user, LocalDateTime.now());

    try {
      return joinPoint.proceed();
    } catch (Exception e) {
      log.error("Error during secret access by {}", user, e);
      throw e;
    }
  }
}
