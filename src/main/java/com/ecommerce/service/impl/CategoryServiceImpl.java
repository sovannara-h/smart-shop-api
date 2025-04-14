package com.ecommerce.service.impl;

import com.ecommerce.exception.CategoryException;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.projection.CategorySummary;
import com.ecommerce.service.interfaces.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<Category> findAllCategories(Pageable pageable) {
    return categoryRepository.findAll(pageable);
  }

  @Override
  @Transactional
  public Category createCategory(Category category) {
    try {
      return categoryRepository.save(category);
    } catch (Exception e) {
      log.error("Unexpected error while creating category", e);
      throw new CategoryException("Error creating category: " + e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> findAllActiveWithProducts() {
    return categoryRepository.findAllActiveWithProducts();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategorySummary> findAllWithProductCount() {
    return categoryRepository.findAllWithProductCount();
  }

  @Override
  @Transactional
  public void addProductToCategory(Category category, Product product) {
    category.addProduct(product);
    categoryRepository.save(category);
  }
}
