package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.exception.UserAlreadyExistsException;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.impl.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userService;

  private User testUser;
  private SignUpRequest signUpRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setPassword("hashedPassword");

    signUpRequest = new SignUpRequest();
    signUpRequest.setEmail("test@example.com");
    signUpRequest.setPassword("password123");
  }

  @Test
  void registerUser_Success() {
    // Given
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    User result = userService.registerUser(signUpRequest);

    // Then
    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    assertEquals("hashedPassword", result.getPassword());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void registerUser_EmailAlreadyExists_ThrowsException() {
    // Given
    when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

    // When/Then
    assertThrows(
        UserAlreadyExistsException.class,
        () -> {
          userService.registerUser(signUpRequest);
        });
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void registerUser_NullRequest_ThrowsException() {
    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          userService.registerUser(null);
        });
  }

  @Test
  void findUserById_Success() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    User result = userService.findUserById(1L);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  void findUserById_NotFound_ThrowsException() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    assertThrows(
        UserNotFoundException.class,
        () -> {
          userService.findUserById(999L);
        });
  }

  @Test
  void findUserByEmail_Success() {
    // Given
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    // When
    User result = userService.findUserByEmail("test@example.com");

    // Then
    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  void findUserByEmail_NotFound_ThrowsException() {
    // Given
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // When/Then
    assertThrows(
        UserNotFoundException.class,
        () -> {
          userService.findUserByEmail("nonexistent@example.com");
        });
  }

  @Test
  void updateUser_Success() {
    // Given
    User updatedUser = new User();
    updatedUser.setEmail("updated@example.com");
    updatedUser.setPassword("newPassword");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
    when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    User result = userService.updateUser(1L, updatedUser);

    // Then
    assertNotNull(result);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateUser_EmailAlreadyExists_ThrowsException() {
    // Given
    User updatedUser = new User();
    updatedUser.setEmail("existing@example.com");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    // When/Then
    assertThrows(
        UserAlreadyExistsException.class,
        () -> {
          userService.updateUser(1L, updatedUser);
        });
  }

  @Test
  void deleteUser_Success() {
    // Given
    when(userRepository.existsById(1L)).thenReturn(true);
    doNothing().when(userRepository).deleteById(1L);

    // When
    userService.deleteUser(1L);

    // Then
    verify(userRepository).deleteById(1L);
  }

  @Test
  void deleteUser_NotFound_ThrowsException() {
    // Given
    when(userRepository.existsById(999L)).thenReturn(false);

    // When/Then
    assertThrows(
        UserNotFoundException.class,
        () -> {
          userService.deleteUser(999L);
        });
  }
}
