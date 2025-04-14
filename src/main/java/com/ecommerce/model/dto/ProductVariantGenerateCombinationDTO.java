package com.ecommerce.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductVariantGenerateCombinationDTO {
  @NotNull(message = "Product ID is required")
  @Positive(message = "Product ID must be positive")
  private Long productId;

  @NotEmpty(message = "Attributes are required")
  private List<Long> attributes;

  @NotNull(message = "Base price is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
  private BigDecimal basePrice;

  @NotNull(message = "Base stock is required")
  @Min(value = 0, message = "Base stock cannot be negative")
  private Integer baseStock;
}
