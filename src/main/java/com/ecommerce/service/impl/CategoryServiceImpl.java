package com.ecommerce.service.impl;

import com.ecommerce.exception.CategoryException;
import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.interfaces.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryServiceImpl(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
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
  public Page<Category> findAllCategories(Pageable pageable) {
    return categoryRepository.findAll(pageable);
  }
}
