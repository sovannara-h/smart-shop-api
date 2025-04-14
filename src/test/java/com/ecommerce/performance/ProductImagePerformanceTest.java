package com.ecommerce.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.model.dto.ProductImageUpsertDTO;
import com.ecommerce.model.entity.ProductImage;
import com.ecommerce.service.impl.ProductImageServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests de performance pour les opérations critiques.
 *
 * <p>Ces tests vérifient que les opérations à grande échelle sont efficaces. Ils sont marqués
 * avec @Tag("performance") pour pouvoir être exécutés séparément.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("performance")
public class ProductImagePerformanceTest {

  @Autowired private ProductImageServiceImpl productImageService;

  private List<ProductImageUpsertDTO> largeBatch;
  private Long productId;

  @BeforeEach
  void setUp() {
    productId = 1L;
    largeBatch = new ArrayList<>();

    // Création d'un lot de 1000 images
    for (int i = 0; i < 1000; i++) {
      ProductImageUpsertDTO dto = new ProductImageUpsertDTO();
      dto.setImageId(UUID.randomUUID().toString());
      dto.setFilename("perf_test_" + i + ".jpg");
      largeBatch.add(dto);
    }
  }

  /**
   * Test de performance pour l'insertion par lots. Vérifie que l'opération est complétée dans un
   * délai raisonnable.
   */
  @Test
  @DisplayName("Doit traiter un grand nombre d'images efficacement")
  void batchProcessingPerformance() {
    // Mesure du temps d'exécution
    long startTime = System.currentTimeMillis();

    List<ProductImage> result =
        productImageService.upsertProductImagesInBatch(largeBatch, productId);

    long endTime = System.currentTimeMillis();
    long executionTime = endTime - startTime;

    // Vérifications
    assertNotNull(result);
    assertEquals(largeBatch.size(), result.size());

    // La performance devrait être inférieure à 10 secondes (ajustable)
    assertTrue(
        executionTime < 10000,
        "L'opération a pris " + executionTime + "ms, ce qui dépasse le seuil de 10000ms");
  }
}
