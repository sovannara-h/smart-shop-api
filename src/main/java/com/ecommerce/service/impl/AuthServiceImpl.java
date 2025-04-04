package com.ecommerce.service.impl;

import com.ecommerce.config.JwtTokenProvider;
import com.ecommerce.service.interfaces.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;

  public AuthServiceImpl(
      AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public String authenticate(String username, String password) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return tokenProvider.generateToken(authentication);
  }
}
