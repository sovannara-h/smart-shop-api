package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.ProductCreateDTO;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   * Creates a new product in an edit session
   *
   * @param sessionId edit session identifier
   * @param dto product creation data
   * @return the created product
   */
  Product createProductInSession(String sessionId, ProductCreateDTO dto);

  /**
   * Updates a product
   *
   * @param id product identifier
   * @param product updated product data
   * @return the updated product
   */
  Product updateProduct(Long id, Product product);

  /**
   * Updates a product in an edit session
   *
   * @param sessionId edit session identifier
   * @param id product identifier
   * @param product updated product data
   * @return the updated product
   */
  Product updateProductInSession(String sessionId, Long id, Product product);

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

  /**
   * Get total count of products
   *
   * @return the total count of products
   */
  long getProductCount();
  // List<Product> searchProducts(ProductSearchCriteria criteria);
}
