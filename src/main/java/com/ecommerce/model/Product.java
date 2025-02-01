package com.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// fields:
// - id
// - name
// - description
// - category
// - price 
// - rating
// - numberOfReviews
// - active
// - attributes
// - interactions
// - version
// - stockQuantity
// - createdAt
// - updatedAt

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private BigDecimal rating;
    private Integer numberOfReviews;
    private Boolean active;
    private String attributes;
    private String interactions;
    private Long version;
    private Integer stockInQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

} 










