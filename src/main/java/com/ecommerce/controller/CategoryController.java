package com.ecommerce.controller;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.service.impl.CategoryServiceImpl;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Category management API")
@Validated
@Slf4j
public class CategoryController {

  private final CategoryServiceImpl categoryServiceImpl;

  public CategoryController(CategoryServiceImpl categoryServiceImpl) {
    this.categoryServiceImpl = categoryServiceImpl;
  }

  @Operation(summary = "Create a new category")
  @Timed(value = "category.creation.time", description = "Category creation time")
  @PostMapping
  @RateLimiter(name = "createCategory")
  public ResponseEntity<ApiResponse<Category>> createCategory(
      @Valid @RequestBody Category category) {
    try {
      Category created = categoryServiceImpl.createCategory(category);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              new ApiResponse<>(
                  true, created, "Category created successfully", null, LocalDateTime.now()));
    } catch (Exception e) {
      log.error("Error while creating category", e);
      return ResponseEntity.badRequest()
          .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
    }
  }

  @Operation(summary = "Get all categories")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<Category>>> getAllCategories(
      @Parameter(description = "Page number (starts at 0)") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of elements per page") @RequestParam(defaultValue = "10")
          int size) {
    try {
      Page<Category> categories = categoryServiceImpl.findAllCategories(PageRequest.of(page, size));
      return ResponseEntity.ok()
          .body(
              new ApiResponse<>(
                  true,
                  categories,
                  "Categories retrieved successfully",
                  null,
                  LocalDateTime.now()));
    } catch (Exception e) {
      log.error("Error retrieving categories", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ApiResponse<>(
                  false, null, null, "Server error: " + e.getMessage(), LocalDateTime.now()));
    }
  }
}
