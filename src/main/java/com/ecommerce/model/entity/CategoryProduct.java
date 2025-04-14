package com.ecommerce.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category_products")
@Getter
@Setter
public class CategoryProduct {
  @EmbeddedId private CategoryProductId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("categoryId")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("productId")
  private Product product;

  @Column(name = "position")
  private Integer position;

  @Column(name = "added_at")
  private LocalDateTime addedAt = LocalDateTime.now();
}
