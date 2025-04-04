package com.ecommerce.repository;

import com.ecommerce.model.entity.ProductVariant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
  Optional<ProductVariant> findBySku(String sku);

  List<ProductVariant> findAllByProductId(Long productId);

  List<ProductVariant> findAllBySkuIn(Collection<String> skus);
}
