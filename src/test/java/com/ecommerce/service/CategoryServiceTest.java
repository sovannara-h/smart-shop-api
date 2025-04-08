package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.exception.CategoryException;
import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.impl.CategoryServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private CategoryServiceImpl categoryService;

  private Category testCategory;
  private Page<Category> categoryPage;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Test Category");
    testCategory.setActive(true);
    testCategory.setCreatedAt(LocalDateTime.now());
    testCategory.setUpdatedAt(LocalDateTime.now());

    List<Category> categories = new ArrayList<>();
    categories.add(testCategory);

    pageable = PageRequest.of(0, 10);
    categoryPage = new PageImpl<>(categories, pageable, categories.size());
  }

  @Test
  void findAllCategories_Success() {
    // Given
    when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

    // When
    Page<Category> result = categoryService.findAllCategories(pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Test Category", result.getContent().get(0).getName());
    verify(categoryRepository).findAll(pageable);
  }

  @Test
  void findAllCategories_EmptyList_Success() {
    // Given
    when(categoryRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

    // When
    Page<Category> result = categoryService.findAllCategories(pageable);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
    verify(categoryRepository).findAll(pageable);
  }

  @Test
  void createCategory_Success() {
    // Given
    Category newCategory = new Category();
    newCategory.setName("New Category");
    newCategory.setActive(true);

    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    // When
    Category result = categoryService.createCategory(newCategory);

    // Then
    assertNotNull(result);
    assertEquals("Test Category", result.getName());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void createCategory_NullCategory_ThrowsException() {
    // When/Then
    assertThrows(
        NullPointerException.class,
        () -> {
          categoryService.createCategory(null);
        });

    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void createCategory_RepositoryException_ThrowsCategoryException() {
    // Given
    Category newCategory = new Category();
    newCategory.setName("New Category");

    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new RuntimeException("Database error"));

    // When/Then
    CategoryException exception =
        assertThrows(
            CategoryException.class,
            () -> {
              categoryService.createCategory(newCategory);
            });

    assertEquals("Error creating category: Database error", exception.getMessage());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void createCategory_WithNullName_ThrowsException() {
    // Given
    Category newCategory = new Category();
    newCategory.setName(null);

    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new RuntimeException("Column 'name' cannot be null"));

    // When/Then
    assertThrows(
        CategoryException.class,
        () -> {
          categoryService.createCategory(newCategory);
        });

    verify(categoryRepository).save(any(Category.class));
  }
}
