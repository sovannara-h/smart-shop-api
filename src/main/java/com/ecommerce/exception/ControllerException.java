package com.ecommerce.exception;

/**
 * Exception spécifique pour les erreurs liées aux contrôleurs. À utiliser quand aucune autre
 * exception spécifique n'est applicable.
 */
public class ControllerException extends RuntimeException {
  public ControllerException(String message) {
    super(message);
  }

  public ControllerException(String message, Throwable cause) {
    super(message, cause);
  }
}
