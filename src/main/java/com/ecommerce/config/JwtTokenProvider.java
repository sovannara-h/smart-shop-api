package com.ecommerce.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Slf4j
@Component
public class JwtTokenProvider {

  private String jwtSecret;
  private final int jwtExpirationInMs = 3600000;
  private final VaultTemplate vaultTemplate;

  @Autowired
  public JwtTokenProvider(VaultTemplate vaultTemplate) {
    this.vaultTemplate = vaultTemplate;
  }

  @PostConstruct
  public void loadSecretFromVault() {
    try {

      VaultResponse response = vaultTemplate.read("secret/data/jwt");

      if (response != null && response.getData() != null) {
        Map<String, Object> data = (Map<String, Object>) response.getData().get("data");
        if (data != null && data.get("current") != null) {
          this.jwtSecret = data.get("current").toString();
          log.info("JWT secret loaded from Vault using KV v2 path");
          return;
        }
      }

      response = vaultTemplate.read("secret/jwt");
      if (response != null
          && response.getData() != null
          && response.getData().get("current") != null) {
        this.jwtSecret = response.getData().get("current").toString();
        log.info("JWT secret loaded from Vault using KV v1 path");
        return;
      }

      this.jwtSecret = "default-jwt-secret-for-dev-only";
      log.warn("No JWT secret found in Vault, using default value");

    } catch (Exception e) {
      this.jwtSecret = "default-jwt-secret-for-dev-only";
      log.error("Error loading JWT secret from Vault", e);
    }
  }

  public String generateToken(Authentication authentication) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(authentication.getName())
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public String getUserIdFromJWT(String token) {
    Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException ex) {
      log.error("Malformed JWT token");
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      log.error("JWT token string is empty");
    }
    return false;
  }

  public synchronized void updateSecret(String newSecret) {
    this.jwtSecret = newSecret;
    log.info("JWT secret updated");
  }
}
