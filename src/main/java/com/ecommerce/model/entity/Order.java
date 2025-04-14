package com.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

/**
 * Entity representing a customer order in the e-commerce system. An order contains order items,
 * customer information, and shipping details. Implements optimistic locking and tracks
 * creation/modification timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "orders")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@BatchSize(size = 20)
public class Order {

  /** Enumeration of possible order statuses representing the order lifecycle. */
  public enum Status {
    /** Initial status when order is created */
    PENDING,
    /** Order is confirmed but not yet shipped */
    CONFIRMED,
    /** Order has been shipped to the customer */
    SHIPPED,
    /** Order has been delivered to the customer */
    DELIVERED,
    /** Order has been cancelled */
    CANCELLED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_date")
  @NotNull(message = "Order date is required")
  @PastOrPresent(message = "Order date cannot be in the future")
  @Index(name = "idx_order_date")
  private LocalDateTime orderDate;

  @Enumerated(EnumType.STRING)
  @NotNull(message = "Status is required")
  @Index(name = "idx_order_status")
  private Status status;

  @Column(name = "total_amount")
  @NotNull(message = "Total amount is required")
  @Min(value = 0, message = "Total amount must be greater than or equal to 0")
  private BigDecimal totalAmount;

  /** Collection of items in this order. Must contain at least one item. */
  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Size(min = 1, message = "Order must contain at least one item")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @BatchSize(size = 50)
  @Builder.Default
  @JsonManagedReference
  private List<OrderItem> items = new ArrayList<>();

  @Version private Long version;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Index(name = "idx_order_created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  @Email(message = "Email must be valid")
  @NotBlank(message = "Email is required")
  @Index(name = "idx_order_customer_email")
  private String customerEmail;

  /**
   * Reference to the user account if the order was placed by a registered user. Can be null for
   * guest checkout orders.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  @NotBlank(message = "Name is required")
  private String shippingName;

  @Column(nullable = false)
  @NotBlank(message = "Address is required")
  private String shippingAddress;

  @Column(nullable = false)
  @NotBlank(message = "Phone is required")
  private String shippingPhone;

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
