package com.ecommerce.exception;

/**
 * Exception thrown when a user registration attempt is made with an email that already exists in
 * the system.
 *
 * <p>This exception indicates a violation of the user email uniqueness constraint.
 */
public class UserAlreadyExistsException extends RuntimeException {

  /**
   * Creates a new instance of UserAlreadyExistsException with the specified message.
   *
   * @param message The message detailing the reason for the exception
   */
  public UserAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Creates a new instance of UserAlreadyExistsException with the specified message and cause.
   *
   * @param message The message detailing the reason for the exception
   * @param cause The underlying cause of the exception
   */
  public UserAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
