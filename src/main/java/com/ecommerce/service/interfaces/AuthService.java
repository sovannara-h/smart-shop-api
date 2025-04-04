package com.ecommerce.service.interfaces;

/** Service for authentication operations */
public interface AuthService {
  /**
   * Authenticates a user with email and password
   *
   * @param email user's email
   * @param password user's password
   * @return JWT token if authentication is successful
   * @throws RuntimeException if authentication fails
   */
  String authenticate(String email, String password);
}
