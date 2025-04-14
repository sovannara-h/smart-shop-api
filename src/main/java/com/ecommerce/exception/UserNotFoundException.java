package com.ecommerce.exception;

/**
 * Exception thrown when a requested user does not exist in the system.
 *
 * <p>This exception is typically thrown when searching for a user by ID or email and no matching
 * user is found.
 */
public class UserNotFoundException extends RuntimeException {

  /**
   * Creates a new instance of UserNotFoundException with the specified message.
   *
   * @param message The message detailing the reason for the exception
   */
  public UserNotFoundException(String message) {
    super(message);
  }

  /**
   * Creates a new instance of UserNotFoundException for a specific user ID.
   *
   * @param userId The ID of the user that was not found
   */
  public UserNotFoundException(Long userId) {
    super("User not found with id: " + userId);
  }
}
