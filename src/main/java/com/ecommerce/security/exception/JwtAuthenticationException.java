package com.ecommerce.security.exception;

import org.springframework.security.core.AuthenticationException;

/** Base exception for JWT authentication errors */
public class JwtAuthenticationException extends AuthenticationException {

  public JwtAuthenticationException(String msg) {
    super(msg);
  }

  public JwtAuthenticationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
