package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.List;

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
import com.ecommerce.model.dto.ProductImagesUpsertDTO;
import com.ecommerce.model.entity.ProductImage;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.ProductImageService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/products/{productId}/images")
@Tag(name = "Images of product")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ProductImageController {
    
    private final ProductImageService productImageService;
    private final ProductRepository productRepository;
    
    @PostMapping
    public ResponseEntity<ApiResponse<List<ProductImage>>> upsertProductImages(
        @PathVariable Long productId,
        @Valid @RequestBody ProductImagesUpsertDTO dto,
        @RequestHeader(value= "X-Session-Id", required = false) String sessionId
    ) {
        try {
            log.info("DTOOO: {}", dto);
            if (dto.getProductImages().stream().anyMatch(img -> img.getImageId() == null)) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, null, "ImageId est requis pour toutes les images", LocalDateTime.now()));
            }
            if (!productRepository.existsById(productId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, null, "Produit non trouvé: " + productId, LocalDateTime.now()));
            }

            

            // if (dto == null || dto.getProductImages() == null) {
            //     return ResponseEntity.badRequest()
            //         .body(new ApiResponse<>(false, null, null, "Les données d'images sont requises", LocalDateTime.now()));
            // }
    
            List<ProductImage> productImages;

            if(sessionId != null) {
                productImages = productImageService.upsertProductImagesInSession(sessionId, dto.getProductImages(), productId);
            } else {
                productImages = productImageService.upsertProductImages(dto.getProductImages(), productId);
            }

            return ResponseEntity.ok(new ApiResponse<>(
                true, productImages, "Upsert product images successfully", null, LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de l'upsert des images du produit : {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }
}
