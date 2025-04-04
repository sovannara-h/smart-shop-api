package com.ecommerce.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ecommerce.service.impl.EditSessionServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionInterceptor implements HandlerInterceptor {

  private final EditSessionServiceImpl editSessionServiceImpl;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String sessionId = request.getHeader("X-Session-Id");

    if (sessionId != null) {
      // Vérifier si la session est valide
      if (!editSessionServiceImpl.isSessionValid(sessionId)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Session expirée ou invalide");
        return false;
      }
    }

    return true;
  }
}
