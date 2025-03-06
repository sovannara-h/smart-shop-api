package com.ecommerce.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// fields:
// - id
// - name
// - description
// - category
// - rating
// - numberOfReviews
// - active
// - interactions
// - version
// - createdAt
// - updatedAt

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Category> categories = new HashSet<>();

    @Column(nullable = false)
    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 100)
    private String name;

    @Column(length = 1000)
    private String description;
    
    // @Column(precision = 10, scale = 2, nullable = false)
    // @NotNull(message = "Price is required.")
    // @Min(value = 0)
    // private BigDecimal price;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "number_of_reviews")
    @Builder.Default
    private Integer numberOfReviews = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "has_variants")
    @Builder.Default
    private Boolean hasVariants = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductVariant> variants = new HashSet<>();
    
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> interactions;

    @Version
    private Long version;

    // @Column(name = "stock_quantity")
    // @NotNull(message = "La quantité en stock est obligatoire")
    // @Min(value = 0)
    // private Integer stockInQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

     @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

} 