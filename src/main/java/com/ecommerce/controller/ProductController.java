package com.ecommerce.controller;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.ProductCreateDTO;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.projection.ProductSummary;
import com.ecommerce.service.interfaces.ProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management API")
@Validated
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "Create a new product")
  @Timed(value = "product.creation.time", description = "Product creation time")
  @PostMapping
  @RateLimiter(name = "createProduct")
  public ResponseEntity<ApiResponse<Product>> createProduct(
      @Valid @RequestBody ProductCreateDTO dto,
      @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
    log.debug("Creating new product from DTO: {}", dto);

    Product product;
    if (sessionId != null) {
      // Creation in edit session
      product = productService.createProductInSession(sessionId, dto);
    } else {
      // Normal creation (outside session)
      product = productService.createProduct(dto);
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                true, product, "Product successfully created", null, LocalDateTime.now()));
  }

  @Operation(summary = "Update a product", description = "Updates an existing product")
  @PutMapping("/{id}")
  @CachePut(value = "products", key = "#id")
  @RateLimiter(name = "updateProduct")
  public ResponseEntity<ApiResponse<Product>> updateProduct(
      @Parameter(description = "Product ID") @PathVariable @Positive Long id,
      @Parameter(description = "Product data") @Valid @RequestBody Product product,
      @Parameter(description = "Product version (ETag)")
          @RequestHeader(value = "If-Match", required = false)
          String ifMatch,
      @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
    log.debug("Starting updateProduct - id: {}, product: {}", id, product);

    Product existingProduct = productService.findProductById(id);
    String currentVersion = String.valueOf(existingProduct.getVersion());

    if (ifMatch != null && !ifMatch.equals(currentVersion)) {
      log.warn("Version conflict - expected: {}, received: {}", currentVersion, ifMatch);
      return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
    }

    if (!id.equals(product.getId())) {
      log.warn("Inconsistent ID - path: {}, body: {}", id, product.getId());
      return ResponseEntity.badRequest().build();
    }

    Product updatedProduct;
    if (sessionId != null) {
      updatedProduct = productService.updateProductInSession(sessionId, id, product);
    } else {
      // Normal update
      updatedProduct = productService.updateProduct(id, product);
    }

    log.info("Product successfully updated - id: {}", id);

    return ResponseEntity.ok()
        .eTag(String.valueOf(updatedProduct.getVersion()))
        .body(
            new ApiResponse<>(
                true, updatedProduct, "Product successfully updated", null, LocalDateTime.now()));
  }

  @Operation(summary = "Delete a product", description = "Deletes an existing product")
  @DeleteMapping("/{id}")
  @CacheEvict(value = "products", key = "#id")
  @RateLimiter(name = "deleteProduct")
  public ResponseEntity<Void> deleteProduct(
      @Parameter(description = "Product ID") @PathVariable @Positive Long id) {
    log.debug("Deleting product - id: {}", id);

    productService.deleteProduct(id);
    log.info("Product successfully deleted - id: {}", id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get all products", description = "Returns a paginated list of all products")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
      @Parameter(description = "Page number (starts at 0)") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of elements per page") @RequestParam(defaultValue = "10")
          int size,
      @RequestParam(defaultValue = "id,asc") String sort) {

    String[] sortParams = sort.split(",");
    String sortField = sortParams[0];
    Sort.Direction direction =
        sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

    Page<Product> products =
        productService.findAllProducts(PageRequest.of(page, size, Sort.by(direction, sortField)));
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true, products, "Products successfully retrieved", null, LocalDateTime.now()));
  }

  @Operation(summary = "Get a product", description = "Returns a product by its ID")
  @GetMapping("/{id}")
  @Cacheable(value = "products", key = "#id")
  @RateLimiter(name = "getProduct")
  public ResponseEntity<ApiResponse<Product>> getProductById(
      @Parameter(description = "Product ID") @PathVariable @Positive Long id) {
    log.debug("Finding product with ID: {}", id);

    Product product = productService.findProductById(id);
    return ResponseEntity.ok()
        .eTag(String.valueOf(product.getVersion()))
        .body(
            new ApiResponse<>(
                true, product, "Product successfully retrieved", null, LocalDateTime.now()));
  }

  @GetMapping("/count")
  public ResponseEntity<ApiResponse<Long>> getProductCount() {
    long count = productService.getProductCount();
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true,
                count,
                "Total product count successfully retrieved",
                null,
                LocalDateTime.now()));
  }

  @GetMapping("/active")
  public ResponseEntity<ApiResponse<Page<ProductSummary>>> getAllActiveProductsOptimized(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,asc") String sort) {

    String[] sortParams = sort.split(",");
    String sortField = sortParams[0];
    Sort.Direction direction =
        sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

    Page<ProductSummary> products =
        productService.findAllActiveProductsOptimized(
            PageRequest.of(page, size, Sort.by(direction, sortField)));

    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true, products, "Products successfully retrieved", null, LocalDateTime.now()));
  }
}
