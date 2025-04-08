package com.ecommerce.exception;

/**
 * Exception spécifique pour les erreurs dans les opérations de service. À utiliser quand une
 * opération de service échoue sans qu'une exception plus spécifique existe.
 */
public class ServiceOperationException extends RuntimeException {
  public ServiceOperationException(String message) {
    super(message);
  }

  public ServiceOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
