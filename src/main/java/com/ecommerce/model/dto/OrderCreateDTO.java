package com.ecommerce.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class OrderCreateDTO {
  @Email @NotBlank private String email;

  private Long userId; // Optionnel

  @NotBlank private String shippingName;

  @NotBlank private String shippingAddress;

  @NotBlank private String shippingPhone;

  @NotEmpty private List<OrderItemDTO> items;
}
