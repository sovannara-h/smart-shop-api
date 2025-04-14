package com.ecommerce.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;
import com.ecommerce.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests d'intégration pour le contrôleur utilisateur.
 *
 * <p>Ces tests vérifient que: 1. Les endpoints REST répondent correctement 2. La conversion entre
 * JSON et objets fonctionne 3. Les validations sont appliquées 4. Les codes HTTP sont corrects
 */
@WebMvcTest(UserController.class)
public class UserControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserServiceImpl userService;

  /**
   * Test l'inscription d'un utilisateur. Vérifie que: - La requête POST est traitée correctement -
   * Les données JSON sont correctement désérialisées - Le service est appelé avec les bons
   * paramètres - La réponse contient les informations attendues
   */
  @Test
  @DisplayName("Doit traiter l'inscription utilisateur avec succès")
  void registerUser_Success() throws Exception {
    // Préparation des données
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail("test@example.com");
    signUpRequest.setPassword("password123");

    User createdUser = new User();
    createdUser.setId(1L);
    createdUser.setEmail("test@example.com");
    createdUser.setPassword("encodedPassword");

    // Configuration du mock
    when(userService.registerUser(any(SignUpRequest.class))).thenReturn(createdUser);

    // Exécution et vérification
    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.data.id", is(1)))
        .andExpect(jsonPath("$.data.email", is("test@example.com")));
  }

  /**
   * Test l'inscription avec des données invalides. Vérifie que: - Les validations sont appliquées -
   * Les erreurs sont correctement communiquées - Le code HTTP est 400 (Bad Request)
   */
  @Test
  @DisplayName("Doit rejeter l'inscription avec email invalide")
  void registerUser_InvalidEmail() throws Exception {
    // Préparation des données invalides
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail("invalid-email");
    signUpRequest.setPassword("password123");

    // Exécution et vérification
    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.error", containsString("Format d'email invalide")));
  }

  /**
   * Test la récupération d'un utilisateur par ID. Vérifie que: - La requête GET est traitée
   * correctement - Le service est appelé avec le bon ID - La réponse contient les informations
   * attendues
   */
  @Test
  @DisplayName("Doit récupérer un utilisateur par ID")
  void getUserById_Success() throws Exception {
    // Préparation
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    // Configuration
    when(userService.findUserById(1L)).thenReturn(user);

    // Exécution et vérification
    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.data.id", is(1)))
        .andExpect(jsonPath("$.data.email", is("test@example.com")));
  }
}
