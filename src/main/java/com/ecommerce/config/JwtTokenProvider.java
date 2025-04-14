package com.ecommerce.config;

import com.ecommerce.security.exception.ExpiredJwtTokenException;
import com.ecommerce.security.exception.InvalidJwtTokenException;
import com.ecommerce.security.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Slf4j
@Component
public class JwtTokenProvider {

  private Key key;
  private final int jwtExpirationInMs = 3600000;
  private final VaultTemplate vaultTemplate;

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  @Autowired
  public JwtTokenProvider(VaultTemplate vaultTemplate) {
    this.vaultTemplate = vaultTemplate;
  }

  @PostConstruct
  public void loadSecretFromVault() {
    try {
      String jwtSecret = loadSecretString();

      // Convert secret to cryptographic key
      byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
      this.key = Keys.hmacShaKeyFor(keyBytes);

    } catch (Exception e) {
      if ("prod".equals(activeProfile)) {
        log.error("Fatal error initializing JWT key", e);
        throw new SecurityException("Failed to initialize JWT key for production environment", e);
      } else {
        // In development, generate a random key
        this.key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        log.warn("Generated random JWT key for development environment");
      }
    }
  }

  /** Loads the JWT secret from Vault and returns its Base64 representation */
  private String loadSecretString() {
    // First try to load the secret from Vault KV v2
    VaultResponse response = vaultTemplate.read("secret/data/jwt");

    if (response != null && response.getData() != null) {
      Map<String, Object> data = (Map<String, Object>) response.getData().get("data");
      if (data != null && data.get("current") != null) {
        log.info("JWT secret loaded from Vault using KV v2 path");
        return data.get("current").toString();
      }
    }

    // Try with Vault KV v1 if KV v2 fails
    response = vaultTemplate.read("secret/jwt");
    if (response != null
        && response.getData() != null
        && response.getData().get("current") != null) {
      log.info("JWT secret loaded from Vault using KV v1 path");
      return response.getData().get("current").toString();
    }

    // If no secret is found in Vault, throw an exception in production
    if ("prod".equals(activeProfile)) {
      throw new SecurityException("No JWT secret found in Vault for production environment");
    } else {
      // In development, return null to force generation of a random key
      return null;
    }
  }

  public String generateToken(Authentication authentication) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(authentication.getName())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String getUserIdFromJWT(String token) {
    Claims claims = getValidatedClaims(token);
    if (claims == null) {
      throw new SecurityException("Invalid JWT token");
    }
    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    try {
      getValidatedClaims(authToken);
      return true;
    } catch (JwtAuthenticationException ex) {
      return false;
    }
  }

  public synchronized void updateSecret(String newSecret) {
    byte[] keyBytes = Decoders.BASE64.decode(newSecret);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    log.info("JWT key updated");
  }

  /**
   * Checks if a token is valid and not expired
   *
   * @param token The JWT token to verify
   * @return The Claims object if the token is valid, null otherwise
   */
  public Claims getValidatedClaims(String token) {
    if (token == null || token.isEmpty()) {
      log.error("JWT token is null or empty");
      throw new InvalidJwtTokenException("Le token JWT est vide ou non fourni");
    }

    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (SignatureException ex) {
      log.error("Invalid JWT signature: {}", ex.getMessage());
      throw new InvalidJwtTokenException("Signature JWT invalide", ex);
    } catch (MalformedJwtException ex) {
      log.error("Malformed JWT token: {}", ex.getMessage());
      throw new InvalidJwtTokenException("Format de token JWT invalide", ex);
    } catch (io.jsonwebtoken.ExpiredJwtException ex) {
      log.error("Expired JWT token: expiration date {}", ex.getClaims().getExpiration());
      throw new ExpiredJwtTokenException("Le token JWT a expiré", ex.getClaims().getExpiration());
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token: {}", ex.getMessage());
      throw new InvalidJwtTokenException("Type de token JWT non pris en charge", ex);
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty: {}", ex.getMessage());
      throw new InvalidJwtTokenException("Contenu du token JWT vide", ex);
    } catch (Exception ex) {
      // Catch-all pour toute autre exception non prévue
      log.error("JWT validation error: {}", ex.getMessage());
      throw new JwtAuthenticationException("Erreur lors de la validation du token JWT", ex);
    }
  }
}
