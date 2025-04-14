package com.ecommerce.security;

import com.ecommerce.model.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Authentication handler that responds with a JSON error message when an unauthenticated user
 * attempts to access a protected resource
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    log.error("Unauthorized error: {}", authException.getMessage());

    // Building the error response
    ApiResponse<?> apiResponse =
        new ApiResponse<>(
            false,
            null,
            authException.getMessage(),
            "Authentication required to access this resource",
            LocalDateTime.now());

    // Configuring the HTTP response
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Writing the JSON response
    objectMapper.writeValue(response.getOutputStream(), apiResponse);
  }
}
