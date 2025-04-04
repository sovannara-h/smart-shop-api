package com.ecommerce.security;

import com.ecommerce.config.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("prod")
@EnableWebSecurity
public class ProdSecurityConfig {

  private final JwtTokenProvider tokenProvider;

  public ProdSecurityConfig(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**", "/api-docs/**", "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.addFilterBefore(
        new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
