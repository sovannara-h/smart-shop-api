package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.service.impl.CategoryServiceImpl;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/categories")
@Tag(name="Categories", description = "Api de gestions des categories")
@Validated
@Slf4j
public class CategoryController {

    private final CategoryServiceImpl categoryServiceImpl;

    public CategoryController(CategoryServiceImpl categoryServiceImpl) {
        this.categoryServiceImpl = categoryServiceImpl;
    }

    @Operation(summary = "Créer une nouvelle catégorie")
    @Timed(value = "category.creation.time", description = "Temps de création d'une catégorie")
    @PostMapping
    @RateLimiter(name = "createCategory")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody Category category) {
        try {
            Category created = categoryServiceImpl.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, created, "Categorie créée avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la création de la catégorie", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

    @Operation(summary = "Récupérer toutes les category")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Category>>> getAllCategories(
        @Parameter(description = "Numéro de page (commence à 0)")
        @RequestParam(defaultValue =  "0") int page,
        @Parameter(description = "Nombre d'éléments par page")
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Category> categories = categoryServiceImpl.findAllCategories(PageRequest.of(page, size));
            return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
            .body(new ApiResponse<>(true, categories, "Commandes récupérés avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des catégories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, null, "Erreur serveur: " + e.getMessage(), LocalDateTime.now()));
        }
    }
}