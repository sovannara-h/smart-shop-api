package com.ecommerce.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating Product entities. Contains only the fields that can be
 * updated by clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDTO {
  @NotBlank(message = "Name is required.")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
  private String name;

  @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
  private String description;

  private Boolean active;
  private Boolean hasVariants;
  private List<Long> categoryIds;
  private Map<String, Object> interactions;
  private Long version;
}
