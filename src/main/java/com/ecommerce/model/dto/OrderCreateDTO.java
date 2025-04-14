package com.ecommerce.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class OrderCreateDTO {
  @Email(message = "Invalid email format")
  @NotBlank(message = "Email is required")
  private String email;

  private Long userId; // Optional

  @NotBlank(message = "Shipping name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String shippingName;

  @NotBlank(message = "Shipping address is required")
  @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
  private String shippingAddress;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
  private String shippingPhone;

  @NotEmpty(message = "Order must contain at least one item")
  @Valid
  private List<OrderItemDTO> items;
}
