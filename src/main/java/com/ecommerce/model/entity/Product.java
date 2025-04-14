package com.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing a product in the e-commerce system. Products can have multiple variants,
 * categories, and images. Implements optimistic locking with version tracking.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@BatchSize(size = 20)
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Name is required.")
  @Size(min = 2, max = 100)
  @Index(name = "idx_product_name")
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(precision = 3, scale = 2)
  @Builder.Default
  private BigDecimal rating = BigDecimal.ZERO;

  @Column(name = "number_of_reviews")
  @Builder.Default
  private Integer numberOfReviews = 0;

  @Column(nullable = false)
  @Builder.Default
  @Index(name = "idx_product_active")
  private Boolean active = false;

  @Column(name = "has_variants")
  @Builder.Default
  private Boolean hasVariants = false;

  /**
   * Collection of product variants with bidirectional relationship. Uses JSON identity info to
   * prevent serialization cycles.
   */
  @JsonIdentityInfo(
      generator = ObjectIdGenerators.PropertyGenerator.class,
      property = "id",
      scope = Product.class)
  @JsonManagedReference
  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @BatchSize(size = 50)
  @Builder.Default
  private Set<ProductVariant> variants = new HashSet<>();

  /**
   * JSON field storing arbitrary customer interaction data. Used for analytics and personalization.
   */
  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> interactions;

  @Version private Long version;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Index(name = "idx_product_created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** Reference to edit session if product is being edited. */
  @Index(name = "idx_product_session_id")
  private String sessionId;

  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 50)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @Builder.Default
  private Set<ProductImage> images = new HashSet<>();

  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @BatchSize(size = 100)
  @Builder.Default
  private Set<CategoryProduct> categoryProducts = new HashSet<>();

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Set<Category> getCategories() {
    return categoryProducts.stream().map(CategoryProduct::getCategory).collect(Collectors.toSet());
  }
}
