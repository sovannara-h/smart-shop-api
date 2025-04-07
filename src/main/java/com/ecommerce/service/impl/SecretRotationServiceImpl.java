package com.ecommerce.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;

import com.ecommerce.config.JwtTokenProvider;
import com.ecommerce.service.interfaces.SecretRotationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecretRotationServiceImpl implements SecretRotationService {
  private final VaultTemplate vaultTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private final NotificationServiceImpl notificationServiceImpl;

  @Value("${secret.rotation.jwt.cron}")
  private String jwtRotationCron;

  private String generateSecureSecret() {
    return UUID.randomUUID().toString() + UUID.randomUUID().toString() + System.currentTimeMillis();
  }

  @Scheduled(cron = "${secret.rotation.jwt.cron}")
  @Override
  public void rotateJwtSecret() {
    try {
      log.info("Starting JWT secret rotation");
      String newSecret = generateSecureSecret();

      Map<String, Object> secretData = vaultTemplate.read("secret/jwt").getData();
      String oldSecret =
          secretData != null && secretData.get("current") != null
              ? secretData.get("current").toString()
              : null;

      Map<String, Object> newData = new HashMap<>();
      if (oldSecret != null) {
        newData.put("previous", oldSecret);
      }
      newData.put("current", newSecret);

      vaultTemplate.write("secret/jwt", newData);

      jwtTokenProvider.updateSecret(newSecret);

      notificationServiceImpl.notifyAdmins("JWT secret rotation completed successfully");
      log.info("JWT secret rotation completed successfully");
    } catch (Exception e) {
      log.error("Error during JWT secret rotation", e);
      notificationServiceImpl.notifyAdmins("ERROR: JWT secret rotation failed");
    }
  }
}
