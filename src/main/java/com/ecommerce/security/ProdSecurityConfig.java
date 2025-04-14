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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@Profile("prod")
@EnableWebSecurity
public class ProdSecurityConfig {

  private final JwtTokenProvider tokenProvider;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;

  public ProdSecurityConfig(
      JwtTokenProvider tokenProvider, JwtAuthenticationEntryPoint authenticationEntryPoint) {
    this.tokenProvider = tokenProvider;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/api/auth/**"))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**", "/api-docs/**", "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(5)
                    .maxSessionsPreventsLogin(false))
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint));

    http.addFilterBefore(
        new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
