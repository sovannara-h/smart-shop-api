package com.ecommerce.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.config.JwtTokenProvider;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests de sécurité pour vérifier que la protection des endpoints fonctionne.
 *
 * <p>Ces tests vérifient que: 1. Les endpoints protégés nécessitent une authentification 2. Les JWT
 * sont correctement validés 3. Les rôles et autorisations sont correctement appliqués
 */
@SpringBootTest
public class SecurityTest {

  @Autowired private WebApplicationContext context;

  @Autowired private JwtTokenProvider tokenProvider;

  private MockMvc mockMvc;
  private String validToken;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    // Création d'un token valide pour les tests
    UserDetails userDetails =
        new User(
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    Authentication auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    validToken = tokenProvider.generateToken(auth);
  }

  /**
   * Test d'accès à un endpoint public. Vérifie que les ressources publiques sont accessibles sans
   * authentification.
   */
  @Test
  @DisplayName("Doit permettre l'accès aux endpoints publics")
  void accessPublicEndpoint() throws Exception {
    mockMvc
        .perform(get("/api/public/info").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * Test d'accès à un endpoint protégé sans authentification. Vérifie que l'accès est refusé avec
   * un statut 401 (non autorisé).
   */
  @Test
  @DisplayName("Doit refuser l'accès aux endpoints protégés sans authentification")
  void accessProtectedEndpointWithoutAuth() throws Exception {
    mockMvc
        .perform(get("/api/users/profile").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  /**
   * Test d'accès à un endpoint protégé avec un token JWT valide. Vérifie que l'accès est autorisé
   * avec un statut 200 (OK).
   */
  @Test
  @DisplayName("Doit permettre l'accès aux endpoints protégés avec token valide")
  void accessProtectedEndpointWithValidToken() throws Exception {
    mockMvc
        .perform(
            get("/api/users/profile")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * Test d'accès à un endpoint protégé avec un token JWT invalide. Vérifie que l'accès est refusé
   * avec un statut 401 (non autorisé).
   */
  @Test
  @DisplayName("Doit refuser l'accès avec token invalide")
  void accessProtectedEndpointWithInvalidToken() throws Exception {
    mockMvc
        .perform(
            get("/api/users/profile")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  /**
   * Test d'accès à un endpoint administrateur sans les autorisations nécessaires. Vérifie que
   * l'accès est refusé avec un statut 403 (interdit).
   */
  @Test
  @DisplayName("Doit refuser l'accès aux endpoints admin pour un utilisateur standard")
  void accessAdminEndpointWithUserRole() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  /**
   * Test de protection CSRF avec token CSRF manquant. Vérifie que les requêtes POST sans token CSRF
   * sont rejetées.
   */
  @Test
  @DisplayName("Doit rejeter les requêtes POST sans token CSRF")
  void postRequestWithoutCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
        .andExpect(status().isForbidden());
  }

  /**
   * Test de protection CSRF avec token CSRF valide. Vérifie que les requêtes POST avec token CSRF
   * sont acceptées.
   */
  @Test
  @DisplayName("Doit accepter les requêtes POST avec token CSRF")
  void postRequestWithCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
        .andExpect(status().isCreated());
  }
}
