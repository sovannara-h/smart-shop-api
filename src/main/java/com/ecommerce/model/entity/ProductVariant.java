package com.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"product", "variantAttributeValues"})
public class ProductVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(unique = true, nullable = false)
  private String sku;

  @Column(precision = 10, scale = 2, nullable = false)
  @NotNull(message = "Le prix est obligatoire")
  @Min(value = 0)
  private BigDecimal price;

  @Column(name = "stock_quantity")
  @NotNull(message = "La quantité en stock est obligatoire")
  @Min(value = 0)
  private Integer stockQuantity;

  @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnoreProperties("productVariant")
  @Builder.Default
  private Set<ProductVariantAttributeValue> variantAttributeValues = new HashSet<>();

  private String sessionId;
}
