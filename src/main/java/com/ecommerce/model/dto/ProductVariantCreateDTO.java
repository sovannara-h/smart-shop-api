package com.ecommerce.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductVariantCreateDTO {
    private List<VariantCreateDTO> productVariants;
}

