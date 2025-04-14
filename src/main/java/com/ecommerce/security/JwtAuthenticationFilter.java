package com.ecommerce.security;

import com.ecommerce.config.JwtTokenProvider;
import com.ecommerce.security.exception.ExpiredJwtTokenException;
import com.ecommerce.security.exception.InvalidJwtTokenException;
import com.ecommerce.security.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);

      if (jwt != null) {
        try {
          Claims claims = tokenProvider.getValidatedClaims(jwt);
          String userId = claims.getSubject();

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

          SecurityContextHolder.getContext().setAuthentication(authentication);

          log.debug("JWT authentication successful for user: {}", userId);
        } catch (ExpiredJwtTokenException ex) {
          log.debug("Expired JWT token for request to {}", request.getRequestURI());
          // On peut ajouter un en-tête spécifique pour informer le client
          response.setHeader("X-JWT-Expired", "true");
        } catch (InvalidJwtTokenException ex) {
          log.debug(
              "Invalid JWT token for request to {}: {}", request.getRequestURI(), ex.getMessage());
        } catch (JwtAuthenticationException ex) {
          log.debug(
              "JWT authentication failed for request to {}: {}",
              request.getRequestURI(),
              ex.getMessage());
        }
      }
    } catch (Exception ex) {
      log.error("Could not set user authentication in security context", ex);
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
