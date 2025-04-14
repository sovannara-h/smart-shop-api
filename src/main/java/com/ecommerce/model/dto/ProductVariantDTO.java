package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for ProductVariant entities. Used to represent product variants in API
 * responses without exposing the entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
  private Long id;
  private String sku;
  private BigDecimal price;
  private Integer quantity;
  private Boolean active;
  private Map<String, String> attributes;
  private String imageUrl;
}
