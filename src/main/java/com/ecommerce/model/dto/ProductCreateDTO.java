package com.ecommerce.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  @NotBlank(message = "Product name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  @Size(max = 2000, message = "Description cannot exceed 2000 characters")
  private String description;

  private Boolean active;

  private List<Long> categories;

  private Boolean hasVariants;

  @NotNull(message = "Base price is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
  private BigDecimal basePrice;

  @Min(value = 0, message = "Stock cannot be negative")
  private Integer baseStock;

  private Map<String, Object> interactions;
}
