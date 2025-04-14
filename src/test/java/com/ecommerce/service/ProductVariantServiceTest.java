package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.ProductVariantGenerateCombinationDTO;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.repository.AttributeValueRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantAttributeValueRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.ecommerce.service.impl.AttributeServiceImpl;
import com.ecommerce.service.impl.EditSessionServiceImpl;
import com.ecommerce.service.impl.ProductVariantServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tests unitaires complets pour ProductVariantServiceImpl.
 *
 * <p>Ces tests couvrent le comportement complexe de génération de combinaisons de variantes et les
 * opérations de persistance associées.
 */
@ExtendWith(MockitoExtension.class)
public class ProductVariantServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private AttributeRepository attributeRepository;

  @Mock private AttributeValueRepository attributeValueRepository;

  @Mock private ProductVariantRepository productVariantRepository;

  @Mock private AttributeServiceImpl attributeService;

  @Mock private JdbcTemplate jdbcTemplate;

  @Mock private EditSessionServiceImpl editSessionService;

  @Mock private ProductVariantAttributeValueRepository productVariantAttributeValueRepository;

  @InjectMocks private ProductVariantServiceImpl productVariantService;

  private Product testProduct;
  private List<Long> attributeIds;
  private ProductVariantGenerateCombinationDTO generateDTO;
  private List<AttributeDTO> attributesWithValues;

  @BeforeEach
  void setUp() {
    // Configuration du produit test
    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setName("Test Product");
    testProduct.setDescription("A test product");
    testProduct.setHasVariants(true);

    // Configuration des IDs d'attributs
    attributeIds = Arrays.asList(1L, 2L);

    // Configuration du DTO de génération de combinaisons
    generateDTO = new ProductVariantGenerateCombinationDTO();
    generateDTO.setProductId(1L);
    generateDTO.setAttributes(attributeIds);
    generateDTO.setBasePrice(new BigDecimal("99.99"));
    generateDTO.setBaseStock(10);

    // Configuration des attributs avec valeurs
    attributesWithValues = new ArrayList<>();

    // Attribut "Couleur"
    AttributeDTO colorAttr = new AttributeDTO();
    colorAttr.setId(1L);
    colorAttr.setName("Couleur");
    List<Map<String, Object>> colorValues = new ArrayList<>();

    Map<String, Object> redValue = new HashMap<>();
    redValue.put("id", 1L);
    redValue.put("value", "Rouge");

    Map<String, Object> blueValue = new HashMap<>();
    blueValue.put("id", 2L);
    blueValue.put("value", "Bleu");

    colorValues.add(redValue);
    colorValues.add(blueValue);
    colorAttr.setValues(colorValues);

    // Attribut "Taille"
    AttributeDTO sizeAttr = new AttributeDTO();
    sizeAttr.setId(2L);
    sizeAttr.setName("Taille");
    List<Map<String, Object>> sizeValues = new ArrayList<>();

    Map<String, Object> sValue = new HashMap<>();
    sValue.put("id", 3L);
    sValue.put("value", "S");

    Map<String, Object> mValue = new HashMap<>();
    mValue.put("id", 4L);
    mValue.put("value", "M");

    sizeValues.add(sValue);
    sizeValues.add(mValue);
    sizeAttr.setValues(sizeValues);

    attributesWithValues.add(colorAttr);
    attributesWithValues.add(sizeAttr);
  }

  /**
   * Test de génération de variantes. Ce test vérifie que toutes les combinaisons possibles
   * d'attributs sont générées et formatées correctement.
   */
  @Test
  @DisplayName("Doit générer toutes les combinaisons de variantes possibles")
  void generateVariants_Success() {
    // Configuration des mocks
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(attributeService.findAttributesWithValuesByIds(attributeIds))
        .thenReturn(attributesWithValues);

    // Liste des variantes existantes (vide pour ce test)
    when(productVariantRepository.findAllByProductId(1L)).thenReturn(new ArrayList<>());

    // Exécution de la méthode
    List<Map<String, Object>> result = productVariantService.generateVariants(generateDTO);

    // Vérifications
    assertNotNull(result);
    assertEquals(4, result.size(), "Il devrait y avoir 4 combinaisons (2 couleurs x 2 tailles)");

    // Vérification du contenu des combinaisons
    Set<String> expectedSkus =
        new HashSet<>(Arrays.asList("1-Bleu-M", "1-Bleu-S", "1-Rouge-M", "1-Rouge-S"));

    Set<String> actualSkus =
        result.stream().map(combo -> (String) combo.get("sku")).collect(Collectors.toSet());

    assertEquals(
        expectedSkus,
        actualSkus,
        "Les SKUs générés ne correspondent pas aux combinaisons attendues");

    // Vérification des prix et stocks
    for (Map<String, Object> variant : result) {
      assertEquals(
          new BigDecimal("99.99"), variant.get("price"), "Le prix de base devrait être appliqué");
      assertEquals(10, variant.get("stockQuantity"), "Le stock de base devrait être appliqué");

      // Vérification des valeurs d'attributs
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> attrValues =
          (List<Map<String, Object>>) variant.get("attributeValues");
      assertEquals(2, attrValues.size(), "Chaque variante devrait avoir 2 attributs");
    }
  }

  /**
   * Test de génération de variantes avec des variantes existantes. Ce test vérifie que les
   * variantes existantes sont correctement identifiées et que leurs propriétés sont préservées.
   */
  @Test
  @DisplayName("Doit préserver les propriétés des variantes existantes")
  void generateVariants_WithExistingVariants() {
    // Configuration des mocks
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(attributeService.findAttributesWithValuesByIds(attributeIds))
        .thenReturn(attributesWithValues);

    // Création d'une variante existante
    ProductVariant existingVariant = new ProductVariant();
    existingVariant.setId(5L);
    existingVariant.setSku("1-Rouge-S");
    existingVariant.setPrice(new BigDecimal("89.99")); // Prix personnalisé
    existingVariant.setStockQuantity(5); // Stock personnalisé

    when(productVariantRepository.findAllByProductId(1L))
        .thenReturn(Collections.singletonList(existingVariant));

    // Exécution de la méthode
    List<Map<String, Object>> result = productVariantService.generateVariants(generateDTO);

    // Vérifications
    assertNotNull(result);
    assertEquals(4, result.size());

    // Recherche de la variante existante dans les résultats
    Optional<Map<String, Object>> existingVariantResult =
        result.stream().filter(v -> v.get("sku").equals("1-Rouge-S")).findFirst();

    assertTrue(
        existingVariantResult.isPresent(),
        "La variante existante devrait être présente dans les résultats");
    Map<String, Object> existingVariantData = existingVariantResult.get();

    // Vérification que l'ID existant est préservé
    assertEquals(
        5L, existingVariantData.get("id"), "L'ID de la variante existante devrait être préservé");

    // Vérification que le prix et le stock personnalisés sont préservés
    assertEquals(
        new BigDecimal("89.99"),
        existingVariantData.get("price"),
        "Le prix personnalisé devrait être préservé");
    assertEquals(
        5, existingVariantData.get("stockQuantity"), "Le stock personnalisé devrait être préservé");
  }

  /**
   * Test de génération de variantes avec produit introuvable. Ce test vérifie que l'exception
   * appropriée est levée lorsque le produit n'existe pas.
   */
  @Test
  @DisplayName("Doit lever une exception si le produit n'existe pas")
  void generateVariants_ProductNotFound() {
    // Configuration du mock pour simuler un produit inexistant
    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    // Vérification que l'exception est levée
    ProductNotFoundException exception =
        assertThrows(
            ProductNotFoundException.class,
            () -> productVariantService.generateVariants(generateDTO));

    assertTrue(exception.getMessage().contains("Product not found"));
  }

  /**
   * Test de génération de variantes sans attributs. Ce test vérifie le comportement lorsqu'aucun
   * attribut n'est fourni.
   */
  @Test
  @DisplayName("Doit gérer le cas où aucun attribut n'est fourni")
  void generateVariants_NoAttributes() {
    // Configuration de l'entrée sans attributs
    generateDTO.setAttributes(Collections.emptyList());

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(attributeService.findAttributesWithValuesByIds(Collections.emptyList()))
        .thenReturn(Collections.emptyList());
    when(productVariantRepository.findAllByProductId(1L)).thenReturn(new ArrayList<>());

    // Exécution de la méthode
    List<Map<String, Object>> result = productVariantService.generateVariants(generateDTO);

    // Vérification que le résultat est vide
    assertNotNull(result);
    assertTrue(result.isEmpty(), "Sans attributs, aucune combinaison ne devrait être générée");
  }

  /**
   * Test pour vérifier le formatage correct du SKU. Ce test vérifie que les SKUs sont générés selon
   * le format attendu.
   */
  @Test
  @DisplayName("Doit générer des SKUs au format attendu")
  void generateSku_CorrectFormat() {
    // Configuration
    Product product = new Product();
    product.setId(1L);

    Map<String, String> combination = new HashMap<>();
    combination.put("Couleur", "Rouge");
    combination.put("Taille", "M");

    // Utiliser la réflexion pour accéder à la méthode privée generateSku
    String sku = "1-M-Rouge"; // Format attendu : "{productId}-{valeur1}-{valeur2}..."

    // Note: Cette vérification est approximative car la méthode generateSku est privée
    // Dans un test réel, on pourrait soit:
    // 1. Utiliser la réflexion pour accéder à la méthode privée
    // 2. Extraire la logique dans une méthode publique pour faciliter les tests
    // 3. Vérifier indirectement via les résultats de generateVariants

    // Ici, nous vérifions indirectement via generateVariants
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(attributeService.findAttributesWithValuesByIds(attributeIds))
        .thenReturn(attributesWithValues);
    when(productVariantRepository.findAllByProductId(1L)).thenReturn(new ArrayList<>());

    List<Map<String, Object>> result = productVariantService.generateVariants(generateDTO);

    // Vérification que les SKUs suivent le format attendu
    Pattern pattern = Pattern.compile("1-[a-zA-Z]+-[a-zA-Z]+");

    for (Map<String, Object> variant : result) {
      String generatedSku = (String) variant.get("sku");
      assertTrue(
          pattern.matcher(generatedSku).matches(),
          "Le SKU '" + generatedSku + "' ne correspond pas au format attendu");
    }
  }
}
