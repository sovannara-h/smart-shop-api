package com.ecommerce.repository;

import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.projection.CategorySummary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Query(
      """
        SELECT DISTINCT c FROM Category c
        LEFT JOIN FETCH c.categoryProducts cp
        LEFT JOIN FETCH cp.product
        WHERE c.active = true
        """)
  List<Category> findAllActiveWithProducts();

  @Query(
      value =
          """
        SELECT c.id, c.name, COUNT(cp.product_id) as product_count
        FROM categories c
        LEFT JOIN category_products cp ON c.id = cp.category_id
        GROUP BY c.id, c.name
        """,
      nativeQuery = true)
  List<CategorySummary> findAllWithProductCount();
}
