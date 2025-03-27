package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductVariantGenerateCombinationDTO {
    private Long productId;
    private List<Long> attributes;
    private BigDecimal basePrice;
    private Integer baseStock;
}
