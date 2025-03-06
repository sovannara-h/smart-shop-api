package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ProductVariantCreateDTO {
    private Long productId;
    private Map<String, List<String>> attributes; // ex: {"Couleur": ["Rouge", "Bleu"]}
    private BigDecimal basePrice;
    private Integer baseStock;
} 