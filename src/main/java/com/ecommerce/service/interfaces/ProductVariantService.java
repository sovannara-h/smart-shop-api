package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.ProductVariantGenerateCombinationDTO;
import com.ecommerce.model.dto.VariantCreateDTO;
import com.ecommerce.model.entity.ProductVariant;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

/** Service for product variant management */
public interface ProductVariantService {

  /**
   * Finds a product variant by its ID
   *
   * @param id variant identifier
   * @return the found product variant
   * @throws RuntimeException if the variant doesn't exist
   */
  ProductVariant findVariantById(Long id);

  /**
   * Finds all variants for a specific product
   *
   * @param productId product identifier
   * @return list of product variants
   */
  List<ProductVariant> findVariantsByProductId(Long productId);

  /**
   * Creates a new product variant
   *
   * @param variant variant data
   * @return the created product variant
   */
  ProductVariant createVariant(ProductVariant variant);

  /**
   * Updates a product variant
   *
   * @param id variant identifier
   * @param variant update data
   * @return the updated product variant
   */
  ProductVariant updateVariant(Long id, ProductVariant variant);

  /**
   * Deletes a product variant
   *
   * @param id variant identifier
   */
  void deleteVariant(Long id);

  /**
   * Generates product variants based on attribute combinations
   *
   * @param dto data transfer object containing product ID and attributes
   * @return list of generated product variants
   */
  List<ProductVariant> generateVariantCombinations(ProductVariantGenerateCombinationDTO dto);

  /**
   * Generates variant data based on attribute combinations
   *
   * @param dto data transfer object containing product ID and attributes
   * @return list of maps containing variant data
   */
  List<Map<String, Object>> generateVariants(ProductVariantGenerateCombinationDTO dto);

  /**
   * Batch creates or updates multiple product variants for a product
   *
   * @param variants list of variant data
   * @param productId product identifier
   * @return list of created/updated product variants
   */
  @Transactional
  List<ProductVariant> batchUpsertVariants(List<VariantCreateDTO> variants, Long productId);

  /**
   * Batch creates or updates multiple product variants within an edit session
   *
   * @param sessionId edit session identifier
   * @param productVariants list of variant data
   * @param productId product identifier
   * @return list of created/updated product variants
   */
  @Transactional
  List<ProductVariant> batchUpsertVariantsInSession(
      String sessionId, List<VariantCreateDTO> productVariants, Long productId);
}
