package com.ecommerce.service.interfaces;

import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;

/** Service for user management */
public interface UserService {
  /**
   * Registers a new user
   *
   * @param signUpRequest registration data
   * @return the created user
   * @throws RuntimeException if the email is already in use
   */
  User registerUser(SignUpRequest signUpRequest);

  /**
   * Finds a user by their ID
   *
   * @param id user identifier
   * @return the found user
   * @throws UserNotFoundException if the user doesn't exist
   */
  User findUserById(Long id) throws UserNotFoundException;

  /**
   * Finds a user by their email
   *
   * @param email user's email
   * @return the found user
   * @throws UserNotFoundException if the user doesn't exist
   */
  User findUserByEmail(String email) throws UserNotFoundException;

  // /**
  //  * Checks if an email is already in use
  //  *
  //  * @param email email to check
  //  * @return true if the email already exists
  //  */
  // boolean existsByEmail(String email);

  /**
   * Updates a user's information
   *
   * @param id user identifier
   * @param user update data
   * @return the updated user
   * @throws UserNotFoundException if the user doesn't exist
   */
  User updateUser(Long id, User user) throws UserNotFoundException;

  /**
   * Deletes a user
   *
   * @param id user identifier
   * @throws UserNotFoundException if the user doesn't exist
   */
  void deleteUser(Long id) throws UserNotFoundException;
}
