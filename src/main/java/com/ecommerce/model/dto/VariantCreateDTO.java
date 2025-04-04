package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class VariantCreateDTO {
  private Long id;
  private String sku;
  private List<Long> attributeValues;
  private BigDecimal price;
  private Integer stockQuantity;
  private String sessionId;
}
