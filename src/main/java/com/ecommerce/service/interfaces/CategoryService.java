package com.ecommerce.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.model.entity.Category;

public interface CategoryService {
    Category createCategory(Category category);
    Page<Category> findAllCategories(Pageable pageable);
}
