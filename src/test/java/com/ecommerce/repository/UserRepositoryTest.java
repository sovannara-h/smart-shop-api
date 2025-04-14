package com.ecommerce.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.model.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Tests d'intégration pour le repository UserRepository.
 *
 * <p>Ces tests vérifient que: 1. Les opérations CRUD fonctionnent correctement 2. Les méthodes
 * personnalisées retournent les résultats attendus 3. Les contraintes de la base de données sont
 * respectées @DataJpaTest configure une base de données en mémoire et active les transactions pour
 * chaque test
 */
@DataJpaTest
public class UserRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private UserRepository userRepository;

  /**
   * Test la méthode findByEmail. Vérifie que l'utilisateur est correctement retrouvé par son email.
   */
  @Test
  @DisplayName("Doit trouver un utilisateur par email")
  void findByEmail_Success() {
    // Préparation - Création d'un utilisateur dans la BD
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    entityManager.persist(user);
    entityManager.flush();

    // Exécution
    Optional<User> found = userRepository.findByEmail("test@example.com");

    // Vérification
    assertTrue(found.isPresent());
    assertEquals("test@example.com", found.get().getEmail());
  }

  /**
   * Test la méthode existsByEmail. Vérifie que la méthode détecte correctement si un email existe.
   */
  @Test
  @DisplayName("Doit vérifier si un email existe")
  void existsByEmail_Success() {
    // Préparation
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    entityManager.persist(user);
    entityManager.flush();

    // Exécution et vérification
    assertTrue(userRepository.existsByEmail("test@example.com"));
    assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
  }

  /**
   * Test la contrainte d'unicité sur l'email. Vérifie qu'une exception est levée si on tente de
   * créer un utilisateur avec un email déjà utilisé.
   */
  @Test
  @DisplayName("Doit empêcher la création d'utilisateurs avec le même email")
  void emailUniquenessConstraint() {
    // Préparation
    User user1 = new User();
    user1.setEmail("duplicate@example.com");
    user1.setPassword("password1");
    entityManager.persist(user1);
    entityManager.flush();

    // Tentative de création d'un second utilisateur avec le même email
    User user2 = new User();
    user2.setEmail("duplicate@example.com");
    user2.setPassword("password2");

    // Vérification que l'exception est levée
    assertThrows(
        Exception.class,
        () -> {
          entityManager.persist(user2);
          entityManager.flush();
        });
  }
}
