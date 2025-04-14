package com.ecommerce.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for Product entities. Used to transfer product data between the server
 * and client, avoiding direct entity serialization in the API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
  private Long id;
  private String name;
  private String description;
  private BigDecimal rating;
  private Integer numberOfReviews;
  private Boolean active;
  private Boolean hasVariants;
  private List<ProductVariantDTO> variants;
  private List<String> categories;
  private List<String> imageUrls;
  private Map<String, Object> interactions;
  private Long version;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
