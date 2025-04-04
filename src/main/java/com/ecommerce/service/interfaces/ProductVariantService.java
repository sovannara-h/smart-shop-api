package com.ecommerce.service.interfaces;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.model.dto.ProductVariantGenerateCombinationDTO;
import com.ecommerce.model.dto.VariantCreateDTO;
import com.ecommerce.model.entity.ProductVariant;

public interface ProductVariantService {
    
    ProductVariant findVariantById(Long id);
    
    List<ProductVariant> findVariantsByProductId(Long productId);
    
    ProductVariant createVariant(ProductVariant variant);
    
    ProductVariant updateVariant(Long id, ProductVariant variant);
    
    void deleteVariant(Long id);
    
    List<ProductVariant> generateVariantCombinations(ProductVariantGenerateCombinationDTO dto);
    
    @Transactional
    List<ProductVariant> batchUpsertVariants(List<VariantCreateDTO> variants, Long productId);
    
    @Transactional
    List<ProductVariant> batchUpsertVariantsInSession(String sessionId, List<VariantCreateDTO> productVariants, Long productId);
}