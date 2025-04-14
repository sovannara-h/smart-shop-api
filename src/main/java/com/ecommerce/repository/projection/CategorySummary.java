package com.ecommerce.repository.projection;

/** Projection interface for category with product count */
public interface CategorySummary {
  Long getId();

  String getName();

  Integer getProductCount();
}
