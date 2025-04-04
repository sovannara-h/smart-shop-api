package com.ecommerce.service.interfaces;

import java.util.List;

import com.ecommerce.model.dto.ProductImageUpsertDTO;
import com.ecommerce.model.entity.ProductImage;

public interface ProductImageService {
  List<ProductImage> upsertProductImagesInBatch(List<ProductImageUpsertDTO> productImages, Long productId);
  
  List<ProductImage> upsertProductImages(List<ProductImageUpsertDTO> productImages, Long productId);
  
  List<ProductImage> upsertProductImagesInSession(String sessionId, List<ProductImageUpsertDTO> productImages, Long productId);
}