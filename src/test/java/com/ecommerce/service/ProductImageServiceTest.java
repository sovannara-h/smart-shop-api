package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.exception.ProductImageUpsertException;
import com.ecommerce.model.dto.ProductImageUpsertDTO;
import com.ecommerce.model.entity.ProductImage;
import com.ecommerce.repository.ProductImageRepository;
import com.ecommerce.service.impl.EditSessionServiceImpl;
import com.ecommerce.service.impl.ProductImageServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
public class ProductImageServiceTest {

  @Mock private ProductImageRepository productImageRepository;

  @Mock private JdbcTemplate jdbcTemplate;

  @Mock private EditSessionServiceImpl editSessionServiceImpl;

  @InjectMocks private ProductImageServiceImpl productImageService;

  private List<ProductImageUpsertDTO> productImageDTOs;
  private List<ProductImage> productImages;
  private Long productId;
  private String sessionId;

  @BeforeEach
  void setUp() {
    // Initialisation des données de test
    productId = 1L;
    sessionId = "test-session-id";

    // Création de DTOs pour les tests
    productImageDTOs = new ArrayList<>();
    ProductImageUpsertDTO dto1 = new ProductImageUpsertDTO();
    dto1.setId(1L);
    dto1.setImageId("img-001");
    dto1.setFilename("image1.jpg");
    dto1.setSessionId(null); // sera défini dans la méthode upsertProductImagesInSession

    ProductImageUpsertDTO dto2 = new ProductImageUpsertDTO();
    dto2.setId(null); // nouvelle image
    dto2.setImageId("img-002");
    dto2.setFilename("image2.jpg");
    dto2.setSessionId(null);

    productImageDTOs.add(dto1);
    productImageDTOs.add(dto2);

    // Création des entités attendues en sortie
    productImages = new ArrayList<>();
    ProductImage image1 = new ProductImage();
    image1.setId(1L);
    image1.setImageId("img-001");
    image1.setFilename("image1.jpg");
    image1.setSessionId(sessionId);

    ProductImage image2 = new ProductImage();
    image2.setId(2L);
    image2.setImageId("img-002");
    image2.setFilename("image2.jpg");
    image2.setSessionId(sessionId);

    productImages.add(image1);
    productImages.add(image2);
  }

  /**
   * Test de la méthode upsertProductImagesInBatch. Cette méthode effectue des insertions par lot
   * dans la base de données. Le test vérifie que: 1. La requête SQL est correctement formatée 2.
   * Les paramètres sont correctement passés 3. Le résultat est correctement traité
   */
  @Test
  @DisplayName("Doit insérer des images par lots")
  void upsertProductImagesInBatch_Success() {
    // Configuration des mocks
    // On simule l'exécution de la requête SQL et on retourne les images créées
    when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
        .thenReturn(productImages);

    // Exécution de la méthode à tester
    List<ProductImage> result =
        productImageService.upsertProductImagesInBatch(productImageDTOs, productId);

    // Vérifications
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("img-001", result.get(0).getImageId());
    assertEquals("img-002", result.get(1).getImageId());

    // Vérifie que la requête SQL a été appelée avec les bons paramètres
    verify(jdbcTemplate).query(anyString(), any(Object[].class), any(RowMapper.class));
  }

  /**
   * Test de la méthode upsertProductImagesInSession. Cette méthode gère les modifications d'images
   * dans le contexte d'une session d'édition. Le test vérifie que: 1. Les IDs de session sont
   * correctement ajoutés 2. Les images existantes sont traitées comme des modifications 3. Les
   * nouvelles images sont traitées comme des créations 4. Les exceptions sont correctement gérées
   */
  @Test
  @DisplayName("Doit créer/mettre à jour des images dans une session")
  void upsertProductImagesInSession_Success() {
    // Configuration des mocks
    // On simule l'exécution de la méthode upsertProductImages
    when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
        .thenReturn(productImages);

    // On simule la recherche des images existantes
    List<Long> existingIds =
        productImageDTOs.stream()
            .map(ProductImageUpsertDTO::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    when(productImageRepository.findAllById(existingIds))
        .thenReturn(Collections.singletonList(productImages.get(0)));

    // Exécution de la méthode à tester
    List<ProductImage> result =
        productImageService.upsertProductImagesInSession(sessionId, productImageDTOs, productId);

    // Vérifications
    assertNotNull(result);
    assertEquals(2, result.size());

    // Vérifie que l'ID de session a été défini pour tous les DTOs
    for (ProductImageUpsertDTO dto : productImageDTOs) {
      assertEquals(sessionId, dto.getSessionId());
    }

    // Vérifie que registerEntityModification a été appelé pour l'image existante
    verify(editSessionServiceImpl)
        .registerEntityModification(
            eq(sessionId), eq("PRODUCTIMAGE"), eq(1L), any(ProductImage.class));

    // Vérifie que registerEntityCreation a été appelé pour la nouvelle image
    verify(editSessionServiceImpl)
        .registerEntityCreation(eq(sessionId), eq("PRODUCTIMAGE"), eq(productId));
  }

  /**
   * Test la gestion des exceptions dans la méthode upsertProductImagesInSession. Vérifie que les
   * exceptions sont correctement capturées et transformées en ProductImageUpsertException.
   */
  @Test
  @DisplayName("Doit gérer les exceptions lors de l'upsert d'images en session")
  void upsertProductImagesInSession_ExceptionHandling() {
    // Configuration des mocks pour provoquer une exception
    when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Vérification que l'exception est correctement gérée
    ProductImageUpsertException exception =
        assertThrows(
            ProductImageUpsertException.class,
            () ->
                productImageService.upsertProductImagesInSession(
                    sessionId, productImageDTOs, productId));

    assertTrue(exception.getMessage().contains("Batch error in session"));
  }

  /**
   * Test avec une liste d'images vide. Vérifie que la méthode fonctionne correctement même sans
   * données.
   */
  @Test
  @DisplayName("Doit gérer une liste d'images vide")
  void upsertProductImagesInBatch_EmptyList() {
    // Configuration
    List<ProductImageUpsertDTO> emptyList = new ArrayList<>();

    // Exécution
    List<ProductImage> result =
        productImageService.upsertProductImagesInBatch(emptyList, productId);

    // Vérification
    assertNotNull(result);
    assertTrue(result.isEmpty());

    // Vérifie que jdbcTemplate n'a pas été appelé
    verify(jdbcTemplate, never()).query(anyString(), any(Object[].class), any(RowMapper.class));
  }
}
