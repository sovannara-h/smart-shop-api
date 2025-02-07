package com.ecommerce.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemDTO {
    @NotNull
    private Long productId;
    
    @NotNull
    @Min(1)
    private Integer quantity;
}
