package com.ecommerce.service.interfaces;

/** Service for security key rotation and management */
public interface SecretRotationService {

  // /**
  //  * Generates a secure random string to be used as a secret
  //  *
  //  * @return the generated secure string
  //  */
  // String generateSecureSecret();

  /**
   * Performs JWT secret rotation according to the configured schedule Replaces the old secret with
   * a new one and updates the JWT token provider
   *
   * @throws RuntimeException if an error occurs during secret rotation
   * @throws IllegalStateException if the new secret cannot be generated or stored
   */
  void rotateJwtSecret();
}
