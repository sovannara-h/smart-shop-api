package com.ecommerce.model.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductListDTO {
  private Long id;
  private String name;
  private String description;
  private BigDecimal rating;
  private Integer numberOfReviews;
  private Boolean active;
  private Boolean hasVariants;
  private String mainImageUrl; // Pour afficher une seule image dans les listes

  // Constructeur pour projection
  public ProductListDTO(
      Long id,
      String name,
      String description,
      BigDecimal rating,
      Integer numberOfReviews,
      Boolean active,
      Boolean hasVariants,
      String mainImageUrl) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.rating = rating;
    this.numberOfReviews = numberOfReviews;
    this.active = active;
    this.hasVariants = hasVariants;
    this.mainImageUrl = mainImageUrl;
  }
}
