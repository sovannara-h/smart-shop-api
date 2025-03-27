package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.ProductVariantCreateDTO;
import com.ecommerce.model.dto.ProductVariantGenerateCombinationDTO;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.ProductVariantServiceImpl;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@Tag(name = "Variantes de produits")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantServiceImpl variantService;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<List<ProductVariant>>> createProductVariants(
        @PathVariable Long productId,
        @Valid @RequestBody ProductVariantCreateDTO dto,
        @RequestHeader(value= "X-Session-Id", required = false) String sessionId
    ) {
        try {
            if (!productRepository.existsById(productId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, null, "Produit non trouvé: " + productId, LocalDateTime.now()));
            }

            List<ProductVariant> productVariants;

            if(sessionId != null) {
                productVariants = variantService.batchUpsertVariantsInSession(sessionId, dto.getProductVariants(), productId);
            } else {
                productVariants = variantService.batchUpsertVariants(dto.getProductVariants(), productId);
            }

            return ResponseEntity.ok(new ApiResponse<>(
                true, productVariants, "Variantes insérées avec succès", null, LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la génération des variantes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }


    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> generateVariants(
        @PathVariable Long productId,
        @Valid @RequestBody ProductVariantGenerateCombinationDTO dto
    ) {
        try {
            dto.setProductId(productId);
            log.info("Données reçues - ProductID: {}, DTO: {}", productId, dto);
            List<Map<String, Object>> variants = variantService.generateVariants(dto);
            return ResponseEntity.ok(new ApiResponse<>(
                true, variants, "Variantes générées avec succès", null, LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la génération des variantes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

} 