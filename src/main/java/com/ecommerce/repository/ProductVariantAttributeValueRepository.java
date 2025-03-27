package com.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.model.entity.ProductVariantAttributeValue;

public interface ProductVariantAttributeValueRepository extends JpaRepository<ProductVariantAttributeValue, Long> {
    
    List<ProductVariantAttributeValue> findByProductVariantId(Long productVariantId);
    
    void deleteByProductVariantId(Long productVariantId);
    
    @Query("SELECT pvav FROM ProductVariantAttributeValue pvav WHERE pvav.productVariant.id = :variantId AND pvav.attributeValue.id IN :attributeValueIds")
    List<ProductVariantAttributeValue> findByProductVariantIdAndAttributeValueIdIn(
        @Param("variantId") Long variantId, 
        @Param("attributeValueIds") List<Long> attributeValueIds
    );
} 