package com.ecommerce.repository;

import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @Query("SELECT p FROM Product p JOIN p.categories c WHERE c = :category")
  Page<Product> findProductsByCategory(@Param("category") Category category, Pageable pageable);

  Page<Product> findByCategoriesContaining(Category category, Pageable pageable);
}
