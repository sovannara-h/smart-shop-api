package com.ecommerce.security.exception;

import java.util.Date;

/** Exception throws when a JWT token is expired */
public class ExpiredJwtTokenException extends JwtAuthenticationException {

  private final Date expiration;

  public ExpiredJwtTokenException(String message, Date expiration) {
    super(message);
    this.expiration = expiration;
  }

  public Date getExpiration() {
    return expiration;
  }
}
