package com.ecommerce.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeDTO {
  @NotBlank(message = "Name is required")
  private String name;

  @NotEmpty(message = "At least one value is required")
  private List<String> values;
}
