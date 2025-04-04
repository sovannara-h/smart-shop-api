package com.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  @JsonBackReference
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @NotNull
  @Min(1)
  private Integer quantity;

  @NotNull
  @Column(name = "unit_price")
  private BigDecimal unitPrice;

  public BigDecimal calculateSubTotal() {
    return unitPrice.multiply(new BigDecimal(quantity));
  }
}
