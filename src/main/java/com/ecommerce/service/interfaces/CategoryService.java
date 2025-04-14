package com.ecommerce.service.interfaces;

import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.projection.CategorySummary;
import java.util.List;
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

  /**
   * Retrieves all active categories with their products pre-loaded to avoid N+1 problem
   *
   * @return list of active categories with products
   */
  List<Category> findAllActiveWithProducts();

  /**
   * Retrieves categories with product count for efficient listing
   *
   * @return list of categories with their product counts
   */
  List<CategorySummary> findAllWithProductCount();

  /**
   * Adds a product to a category using the optimized structure
   *
   * @param category the category
   * @param product the product to add
   */
  void addProductToCategory(Category category, Product product);
}
