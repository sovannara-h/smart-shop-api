package com.ecommerce.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.model.dto.ProductCreateDTO;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;

/** Service for product management */
public interface ProductService {
  /**
   * Creates a new product
   *
   * @param dto product creation data
   * @return the created product
   */
  Product createProduct(ProductCreateDTO dto);

  /**
   * Updates a product
   *
   * @param id product identifier
   * @param product updated product data
   * @return the updated product
   */
  Product updateProduct(Long id, Product product);

  /**
   * Deletes a product
   *
   * @param id product identifier
   */
  void deleteProduct(Long id);

  /**
   * Retrieves all products with pagination
   *
   * @param pageable pagination information
   * @return paginated list of products
   */
  Page<Product> findAllProducts(Pageable pageable);

  /**
   * Finds a product by its ID
   *
   * @param id product identifier
   * @return the found product
   */
  Product findProductById(Long id);

  /**
   * Finds products by category with pagination
   *
   * @param category category to filter by
   * @param pageable pagination information
   * @return paginated list of products in the specified category
   */
  Page<Product> findProductsByCategory(Category category, Pageable pageable);
  // List<Product> searchProducts(ProductSearchCriteria criteria);
}
