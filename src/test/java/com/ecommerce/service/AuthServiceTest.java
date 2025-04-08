package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.config.JwtTokenProvider;
import com.ecommerce.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtTokenProvider tokenProvider;

  @Mock private Authentication authentication;

  @InjectMocks private AuthServiceImpl authService;

  private String username;
  private String password;
  private String jwtToken;

  @BeforeEach
  void setUp() {
    username = "test@example.com";
    password = "password123";
    jwtToken = "test.jwt.token";
  }

  @Test
  void authenticate_Success() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(tokenProvider.generateToken(authentication)).thenReturn(jwtToken);

    // When
    String result = authService.authenticate(username, password);

    // Then
    assertNotNull(result);
    assertEquals(jwtToken, result);
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(tokenProvider).generateToken(authentication);
  }

  @Test
  void authenticate_BadCredentials_ThrowsException() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When/Then
    BadCredentialsException exception =
        assertThrows(
            BadCredentialsException.class,
            () -> {
              authService.authenticate(username, password);
            });

    assertEquals("Bad credentials", exception.getMessage());
  }

  @Test
  void authenticate_UserNotFound_ThrowsException() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new UsernameNotFoundException("User not found"));

    // When/Then
    UsernameNotFoundException exception =
        assertThrows(
            UsernameNotFoundException.class,
            () -> {
              authService.authenticate(username, password);
            });

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void authenticate_OtherAuthenticationError_ThrowsException() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new AuthenticationException("Authentication error") {});

    // When/Then
    AuthenticationException exception =
        assertThrows(
            AuthenticationException.class,
            () -> {
              authService.authenticate(username, password);
            });

    assertEquals("Authentication error", exception.getMessage());
  }

  @Test
  void authenticate_NullUsername_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              authService.authenticate(null, password);
            });

    assertEquals("Null username", exception.getMessage());
  }

  @Test
  void authenticate_EmptyUsername_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              authService.authenticate("", password);
            });

    assertEquals("Empty username", exception.getMessage());
  }

  @Test
  void authenticate_NullPassword_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              authService.authenticate(username, null);
            });

    assertEquals("Null password", exception.getMessage());
  }
}
