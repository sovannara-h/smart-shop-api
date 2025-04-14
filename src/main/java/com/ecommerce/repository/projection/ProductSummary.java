package com.ecommerce.repository.projection;

import java.math.BigDecimal;

public interface ProductSummary {
  Long getId();

  String getName();

  String getDescription();

  BigDecimal getRating();

  Integer getNumberOfReviews();

  Boolean getActive();

  Boolean getHasVariants();

  String getMainImageUrl();
}
