package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.ProductImageUpsertDTO;
import com.ecommerce.model.entity.ProductImage;
import java.util.List;

/** Service for product image management */
public interface ProductImageService {
  /**
   * Batch creates or updates product images for a specific product
   *
   * @param productImages list of product image data
   * @param productId product identifier
   * @return list of created/updated product images
   */
  List<ProductImage> upsertProductImagesInBatch(
      List<ProductImageUpsertDTO> productImages, Long productId);

  /**
   * Creates or updates product images for a specific product
   *
   * @param productImages list of product image data
   * @param productId product identifier
   * @return list of created/updated product images
   */
  List<ProductImage> upsertProductImages(List<ProductImageUpsertDTO> productImages, Long productId);

  /**
   * Creates or updates product images within an edit session
   *
   * @param sessionId edit session identifier
   * @param productImages list of product image data
   * @param productId product identifier
   * @return list of created/updated product images
   */
  List<ProductImage> upsertProductImagesInSession(
      String sessionId, List<ProductImageUpsertDTO> productImages, Long productId);
}
