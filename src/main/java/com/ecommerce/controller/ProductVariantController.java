package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.ProductVariantCreateDTO;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.service.impl.ProductVariantServiceImpl;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products/{productId}/variants")
@Tag(name = "Variantes de produits")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantServiceImpl variantService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<ProductVariant>>> generateVariants(
        @PathVariable Long productId,
        @Valid @RequestBody ProductVariantCreateDTO dto
    ) {
        try {
            List<ProductVariant> variants = variantService.generateAndSaveVariants(dto);
            return ResponseEntity.ok(new ApiResponse<>(
                true, variants, "Variantes générées avec succès", null, LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la génération des variantes", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }
} 