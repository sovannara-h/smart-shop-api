package com.ecommerce.service.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.CategoryException;
import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.interfaces.CategoryService;

import lombok.extern.slf4j.Slf4j;


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
            log.error("Erreur inattendue lors de la création de la catégorie", e);
            throw new CategoryException("Erreur lors de la category: " + e.getMessage());
        }
    }

    @Override
    public Page<Category> findAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
}
