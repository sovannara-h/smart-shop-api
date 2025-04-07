package com.ecommerce.service.interfaces;

import com.ecommerce.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Service for product category management */
public interface CategoryService {
  /**
   * Creates a new product category
   *
   * @param category category data
   * @return the created category
   */
  Category createCategory(Category category);

  /**
   * Retrieves all categories with pagination
   *
   * @param pageable pagination information
   * @return paginated list of categories
   */
  Page<Category> findAllCategories(Pageable pageable);
}
