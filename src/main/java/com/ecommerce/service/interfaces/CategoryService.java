package com.ecommerce.service.interfaces;

import com.ecommerce.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
  Category createCategory(Category category);

  Page<Category> findAllCategories(Pageable pageable);
}
