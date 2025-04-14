package com.ecommerce.security.exception;

/** Exception thrown when a JWT token is invalid (incorrect format, invalid signature, etc.) */
public class InvalidJwtTokenException extends JwtAuthenticationException {

  public InvalidJwtTokenException(String message) {
    super(message);
  }

  public InvalidJwtTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
