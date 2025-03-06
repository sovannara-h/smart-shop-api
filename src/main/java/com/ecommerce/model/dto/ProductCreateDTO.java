package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
    private String name;
    private String description;
    private Boolean active;
    private List<Long> categories;
    private Boolean hasVariants;
    private BigDecimal basePrice;
    private Integer baseStock;
    private Map<String, Object> interactions;
}

