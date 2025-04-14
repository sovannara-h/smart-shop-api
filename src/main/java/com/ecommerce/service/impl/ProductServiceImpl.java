package com.ecommerce.service.impl;

import com.ecommerce.exception.CategoryNotFoundException;
import com.ecommerce.exception.ProductConcurrencyException;
import com.ecommerce.exception.ProductException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.exception.ProductValidationException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.model.dto.ProductCreateDTO;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.projection.ProductSummary;
import com.ecommerce.service.interfaces.EditSessionService;
import com.ecommerce.service.interfaces.ProductService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the product management service. Provides CRUD functionality for products and
 * handles edit sessions.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private static final String PRODUCT_NOT_FOUND_FORMAT = "Produit avec l'ID %d non trouvé";
  private static final String ENTITY_TYPE_PRODUCT = "PRODUCT";
  private static final String ERROR_UPDATING_FORMAT = "Error updating: %s";
  private static final String ERROR_CREATING_FORMAT = "Error creating product: %s";
  private static final String DATA_INTEGRITY_ERROR = "A product with these details already exists";
  private static final String DELETE_REFERENCE_ERROR =
      "Impossible de supprimer le produit car il est référencé par d'autres entités";
  private static final String ERROR_DELETING_FORMAT = "Erreur lors de la suppression: %s";

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final EditSessionService editSessionService;

  /**
   * Validates a product entity against business rules.
   *
   * @param product The product to validate
   * @throws ProductValidationException If validation fails
   */
  private void validateProduct(Product product) {
    List<String> errors = new ArrayList<>();

    if (Boolean.TRUE.equals(product.getHasVariants()) && product.getVariants().isEmpty()) {
      errors.add("Product must have at least one variant");
    }

    if (!errors.isEmpty()) {
      throw new ProductValidationException(String.join(", ", errors));
    }
  }

  /**
   * Updates an existing product's fields with data from an updated product.
   *
   * @param existing The existing product to update
   * @param updated The product containing updated data
   */
  private void updateProductFields(Product existing, Product updated) {
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());

    updateProductCategories(existing, updated.getCategories());

    existing.setActive(updated.getActive());
    existing.setHasVariants(updated.getHasVariants());
  }

  /**
   * Updates the categories associated with a product.
   *
   * @param product The product to update
   * @param categories The new set of categories
   */
  private void updateProductCategories(Product product, Set<Category> categories) {
    if (categories != null) {
      product.getCategoryProducts().clear();
      categories.forEach(category -> category.addProduct(product));
    }
  }

  /**
   * Populates a product entity from a DTO.
   *
   * @param product The product to populate
   * @param dto The DTO containing product data
   */
  private void populateProductFromDTO(Product product, ProductCreateDTO dto) {
    product.setName(dto.getName());
    product.setDescription(dto.getDescription());
    product.setActive(dto.getActive() != null ? dto.getActive() : true);
    product.setHasVariants(dto.getHasVariants());
  }

  /**
   * Associates categories with a product.
   *
   * @param product The product to associate categories with
   * @param categoryIds The IDs of categories to associate
   * @throws CategoryNotFoundException If any category is not found
   */
  private void associateCategoriesWithProduct(Product product, List<Long> categoryIds) {
    if (categoryIds != null && !categoryIds.isEmpty()) {
      List<Category> categories = categoryRepository.findAllById(categoryIds);

      // Verify all categories were found
      if (categories.size() != categoryIds.size()) {
        Set<Long> foundIds = categories.stream().map(Category::getId).collect(Collectors.toSet());

        List<Long> notFoundIds =
            categoryIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());

        throw new CategoryNotFoundException(
            "Categories not found with IDs: "
                + String.join(
                    ", ", notFoundIds.stream().map(String::valueOf).collect(Collectors.toList())));
      }

      categories.forEach(category -> category.addProduct(product));
    }
  }

  /**
   * Handles common exceptions during product operations.
   *
   * @param e The exception that occurred
   * @param errorMessage The error message format
   * @param logMessage The log message
   * @param id The product ID (if applicable)
   * @throws ProductException The mapped exception
   */
  private void handleProductOperationException(
      Exception e, String errorMessage, String logMessage, Long id) {
    if (e instanceof ProductNotFoundException || e instanceof ProductConcurrencyException) {
      throw (RuntimeException) e;
    } else if (e instanceof DataIntegrityViolationException) {
      log.error("Data integrity error for product {}: {}", id, e.getMessage());
      throw new ProductException(DATA_INTEGRITY_ERROR);
    } else {
      log.error(logMessage, id, e);
      throw new ProductException(String.format(errorMessage, e.getMessage()));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Product findProductById(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("ID must be positive");
    }
    return productRepository
        .findByIdWithDetails(id)
        .orElseThrow(
            () -> new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND_FORMAT, id)));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> findAllProducts(Pageable pageable) {
    return productRepository.findAllWithBasicDetails(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Product> findProductsByCategory(Category category, Pageable pageable) {
    return productRepository.findProductsByCategoryWithDetails(category, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public long getProductCount() {
    return productRepository.countProducts();
  }

  @Override
  @Transactional
  public Product updateProductInSession(String sessionId, Long id, Product product) {
    Product existingProduct = findProductById(id);

    if (!sessionId.equals(existingProduct.getSessionId())) {
      throw new IllegalStateException("Product is not in the specified session");
    }

    validateProduct(product);
    updateProductFields(existingProduct, product);
    existingProduct.setSessionId(sessionId);

    return productRepository.save(existingProduct);
  }

  @Override
  @Transactional
  public Product createProductInSession(String sessionId, ProductCreateDTO dto) {
    if (!editSessionService.isSessionValid(sessionId)) {
      throw new SessionExpiredException("Session has expired or does not exist");
    }

    Product product = new Product();
    populateProductFromDTO(product, dto);
    product.setActive(false); // Products created in session start as inactive
    product.setSessionId(sessionId);

    validateProduct(product);
    Product savedProduct = productRepository.save(product);

    editSessionService.registerEntityCreation(sessionId, ENTITY_TYPE_PRODUCT, savedProduct.getId());

    return savedProduct;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Product createProduct(ProductCreateDTO dto) {
    log.debug("Creating new product: {}", dto.getName());

    try {
      Product product = new Product();
      populateProductFromDTO(product, dto);
      associateCategoriesWithProduct(product, dto.getCategories());

      validateProduct(product);
      return productRepository.save(product);
    } catch (Exception e) {
      handleProductOperationException(
          e, ERROR_CREATING_FORMAT, "Unexpected error while creating product", null);
      throw new IllegalStateException("Cette ligne ne devrait jamais être atteinte");
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Product updateProduct(Long id, Product product) {
    log.debug("Updating product with ID: {}", id);
    validateProduct(product);

    try {
      Product existingProduct = findProductById(id);

      if (!existingProduct.getVersion().equals(product.getVersion())) {
        throw new ProductConcurrencyException("Product has been modified by another user");
      }

      updateProductFields(existingProduct, product);
      return productRepository.save(existingProduct);
    } catch (Exception e) {
      handleProductOperationException(e, ERROR_UPDATING_FORMAT, "Error updating product {}", id);
      throw new IllegalStateException("Cette ligne ne devrait jamais être atteinte");
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND_FORMAT, id));
    }

    try {
      productRepository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      log.error("Erreur d'intégrité des données lors de la suppression du produit {}", id, e);
      throw new ProductException(DELETE_REFERENCE_ERROR);
    } catch (Exception e) {
      log.error("Erreur lors de la suppression du produit {}", id, e);
      throw new ProductException(String.format(ERROR_DELETING_FORMAT, e.getMessage()));
    }
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
  public Page<ProductSummary> findAllActiveProductsOptimized(Pageable pageable) {
    return productRepository.findAllActiveProductsOptimized(pageable);
  }
}
