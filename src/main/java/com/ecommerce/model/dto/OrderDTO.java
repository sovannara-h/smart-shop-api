package com.ecommerce.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
  private Long id;
  private String customerEmail;
  private String shippingName;
  private String shippingAddress;
  private String shippingPhone;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<OrderItemDTO> items;
}
