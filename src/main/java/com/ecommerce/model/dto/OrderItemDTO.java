package com.ecommerce.model.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
  private Long id;
  private Long productId;
  private Long productVariantId;
  private String productName;
  private String variantName;
  private Integer quantity;
  private BigDecimal price;
}
