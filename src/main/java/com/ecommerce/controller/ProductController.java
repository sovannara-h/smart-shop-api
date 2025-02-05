package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.exception.ProductException;
import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.entity.Product;
import com.ecommerce.service.impl.ProductServiceImpl;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produits", description = "API de gestion des produits")
@Validated
@Slf4j
public class ProductController {

    private final ProductServiceImpl productService;

    public ProductController(ProductServiceImpl productService) {
        this.productService = productService;
    }

    @Operation(summary = "Créer un nouveau produit")
    @Timed(value = "product.creation.time", description = "Temps de création d'un produit")
    @PostMapping
    @RateLimiter(name = "createProduct", fallbackMethod = "createProductFallback")
    @Bulkhead(name = "createProduct", fallbackMethod = "createProductFallback")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody Product product) {
        try {
            Product created = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, created, "Produit créé avec succès", null, LocalDateTime.now()));
        } catch (ProductException e) {
            log.error("Erreur lors de la création du produit", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

    private ResponseEntity<ApiResponse<Product>> createProductFallback(Product product, Exception ex) {
        log.warn("Fallback appelé pour la création du produit", ex);
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ApiResponse<>(false, null, "Service temporairement surchargé", ex.getMessage(), LocalDateTime.now()));
    }

    @Operation(summary = "Mettre à jour un produit", description = "Met à jour un produit existant")
    @PutMapping("/{id}")
    @CachePut(value = "products", key = "#id")
    @RateLimiter(name = "updateProduct")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
        @Parameter(description = "ID du produit") 
        @PathVariable @Positive Long id,
        
        @Parameter(description = "Données du produit") 
        @Valid @RequestBody Product product,
        
        @Parameter(description = "Version du produit (ETag)") 
        @RequestHeader(value = "If-Match", required = false) String ifMatch
    ) {
        log.debug("Début updateProduct - id: {}, product: {}", id, product);
        
        try {

            Product existingProduct = productService.findProductById(id);
            String currentVersion = String.valueOf(existingProduct.getVersion());
            
            if (ifMatch != null && !ifMatch.equals(currentVersion)) {
                log.warn("Conflit de version - attendu: {}, reçu: {}", currentVersion, ifMatch);
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
            }
            
            if (!id.equals(product.getId())) {
                log.warn("ID incohérent - path: {}, body: {}", id, product.getId());
                return ResponseEntity.badRequest().build();
            }
            
            Product updatedProduct = productService.updateProduct(id, product);
            
            log.info("Produit mis à jour avec succès - id: {}", id);
            
            return ResponseEntity.ok()
                .eTag(String.valueOf(updatedProduct.getVersion()))
                .body(new ApiResponse<>(true, updatedProduct, "Produit créé avec succès", null, LocalDateTime.now()));
                
        } catch (ProductException e) {
            log.warn("Produit non trouvé - id: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (OptimisticLockingFailureException e) {
            log.warn("Conflit de version lors de la mise à jour - id: {}", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du produit - id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Operation(summary = "Supprimer un produit", description = "Supprime un produit existant")
    @DeleteMapping("/{id}")
    @CacheEvict(value = "products", key = "#id")
    @RateLimiter(name = "deleteProduct")
    public ResponseEntity<Void> deleteProduct(
        @Parameter(description = "ID du produit")
        @PathVariable @Positive Long id
    ) {
        log.debug("Suppression du produit - id: {}", id);
        
        try {
            productService.deleteProduct(id);
            log.info("Produit supprimé avec succès - id: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (ProductException e) {
            log.warn("Produit non trouvé lors de la suppression - id: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du produit - id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    @Operation(summary = "Récupérer tous les produits", description = "Retourne une liste paginée de tous les produits")
    @GetMapping
    @Cacheable(value = "products", key = "#page + '-' + #size", unless = "#result.body.data.content.isEmpty()")
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Product> products = productService.findAllProducts(PageRequest.of(page, size));
            return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
            .body(new ApiResponse<>(true, products, "Produits récupérés avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, null, "Erreur serveur: " + e.getMessage(), LocalDateTime.now()));
        }
    }


    

    @Operation(summary = "Récupérer un produit", description = "Retourne un produit par son ID")
    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id", unless = "#result.statusCode.is4xxClientError()")
    @RateLimiter(name = "getProduct")
    public ResponseEntity<ApiResponse<Product>> getProductById(
        @Parameter(description = "ID du produit")
        @PathVariable @Positive Long id
    ) {
        log.debug("Recherche du produit avec l'ID: {}", id);
        
        try {
            Product product = productService.findProductById(id);
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .eTag(String.valueOf(product.getVersion()))
                .body(new ApiResponse<>(true, product, "Produits récupérés avec succès", null, LocalDateTime.now()));
                
        } catch (ProductException e) {
            log.warn("Produit non trouvé: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du produit: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

} 