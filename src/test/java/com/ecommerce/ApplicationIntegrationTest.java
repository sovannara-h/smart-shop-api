package com.ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;
import com.ecommerce.service.interfaces.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration complets avec Spring Boot.
 *
 * <p>Ces tests démarrent l'application complète avec une base de données en mémoire et testent
 * l'intégration de tous les composants ensemble. @ActiveProfiles("test") charge les configurations
 * spécifiques aux tests @Transactional fait rollback des transactions après chaque test
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ApplicationIntegrationTest {

  @Autowired private UserService userService;

  /** Test le flux complet d'inscription et de récupération d'un utilisateur. */
  @Test
  @DisplayName("Doit permettre l'inscription et la récupération d'un utilisateur")
  void userRegistrationFlow() {
    // Préparation
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail("integration@example.com");
    signUpRequest.setPassword("securePassword123");

    // Inscription
    User createdUser = userService.registerUser(signUpRequest);
    assertNotNull(createdUser);
    assertNotNull(createdUser.getId());
    assertEquals("integration@example.com", createdUser.getEmail());

    // Récupération par ID
    User retrievedById = userService.findUserById(createdUser.getId());
    assertEquals(createdUser.getId(), retrievedById.getId());

    // Récupération par email
    User retrievedByEmail = userService.findUserByEmail("integration@example.com");
    assertEquals(createdUser.getId(), retrievedByEmail.getId());
  }
}
