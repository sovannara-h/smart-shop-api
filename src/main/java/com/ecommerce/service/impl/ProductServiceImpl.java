package com.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.ecommerce.service.interfaces.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final EditSessionServiceImpl editSessionServiceImpl;

  private void _validateProduct(Product product) {
    List<String> errors = new ArrayList<>();

    if (Boolean.TRUE.equals(product.getHasVariants()) && product.getVariants().isEmpty()) {
      errors.add("Product must have at least one variant");
    }

    if (!errors.isEmpty()) {
      throw new ProductValidationException(String.join(", ", errors));
    }
  }

  private void _updateProductFields(Product existing, Product updated) {
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());
    existing.setCategories(updated.getCategories());
    existing.setActive(updated.getActive());
    existing.setHasVariants(updated.getHasVariants());
  }

  @Override
  @Transactional(readOnly=true)
  public Product findProductById(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("ID must be positive");
    }
    return productRepository.findById(id).orElseThrow(() -> new ProductException("id"));
  }

  @Override
  @Transactional(readOnly=true)
  public Page<Product> findAllProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly=true)
  public Page<Product> findProductsByCategory(Category category, Pageable pageable) {
    return productRepository.findProductsByCategory(category, pageable);
  }


  @Transactional(readOnly=true)
  public long getProductCount() {
    return productRepository.count();
  }

  @Transactional
  public Product updateProductInSession(String sessionId, Long id, Product product) {
    Product existingProduct = findProductById(id);

    if (!sessionId.equals(existingProduct.getSessionId())) {
      throw new IllegalStateException("Product is not in the specified session");
    }

    _updateProductFields(existingProduct, product);

    existingProduct.setSessionId(sessionId);

    return productRepository.save(existingProduct);
  }

  @Transactional
  public Product createProductInSession(String sessionId, ProductCreateDTO dto) {
    if (!editSessionServiceImpl.isSessionValid(sessionId)) {
      throw new SessionExpiredException("Session has expired or does not exist");
    }

    Product product = new Product();
    product.setName(dto.getName());
    product.setDescription(dto.getDescription());
    product.setActive(false);

    product.setSessionId(sessionId);

    Product savedProduct = productRepository.save(product);

    editSessionServiceImpl.registerEntityCreation(sessionId, "PRODUCT", savedProduct.getId());

    return savedProduct;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Product createProduct(ProductCreateDTO dto) {
    log.debug("Creating new product: {}", dto.getName());

    try {
      Product product = new Product();
      product.setName(dto.getName());
      product.setDescription(dto.getDescription());
      product.setActive(dto.getActive() != null ? dto.getActive() : true);
      product.setHasVariants(dto.getHasVariants());

      Set<Category> categories =
          dto.getCategories().stream()
              .<Category>map(
                  id ->
                      categoryRepository
                          .findById(id)
                          .orElseThrow(
                              () -> new CategoryNotFoundException("Category not found: " + id)))
              .collect(Collectors.toSet());
      product.setCategories(categories);

      _validateProduct(product);
      return productRepository.save(product);
    } catch (DataIntegrityViolationException e) {
      log.error("Data integrity error while creating product", e);
      throw new ProductException("A product with these details already exists");
    } catch (Exception e) {
      log.error("Unexpected error while creating product", e);
      throw new ProductException("Error creating product: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public Product updateProduct(Long id, Product product) {
    log.debug("Updating product with ID: {}", id);
    _validateProduct(product);

    try {
      Product existingProduct = findProductById(id);

      if (!existingProduct.getVersion().equals(product.getVersion())) {
        throw new ProductConcurrencyException("Product has been modified by another user");
      }

      _updateProductFields(existingProduct, product);
      return productRepository.save(existingProduct);
    } catch (ProductNotFoundException | ProductConcurrencyException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error updating product {}", id, e);
      throw new ProductException("Error updating: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public void deleteProduct(Long id) {
    try {
      productRepository.deleteById(id);
    } catch (Exception e) {
      throw new ProductException(e.getMessage());
    }
  }

}
