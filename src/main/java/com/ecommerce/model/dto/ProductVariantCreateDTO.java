package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductVariantCreateDTO {
    private Long productId;
    private List<Long> attributes; // ex: {"Couleur": ["Rouge", "Bleu"]}
    private BigDecimal basePrice;
    private Integer baseStock;
} 