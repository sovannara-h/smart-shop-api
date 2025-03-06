package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;

@Data
public class ProductVariantDTO {
    private String sku;
    private Map<String, String> attributeValues; // ex: {"Couleur": "Rouge"}
    private BigDecimal price;
    private Integer stockQuantity;
} 