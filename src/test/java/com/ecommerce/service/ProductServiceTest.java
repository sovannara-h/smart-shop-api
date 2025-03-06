package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.ecommerce.exception.ProductException;
import com.ecommerce.exception.ProductValidationException;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .hasVariants(true)
            .variants(new HashSet<>(Arrays.asList(
                ProductVariant.builder()
                    .sku("TEST-1")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(10)
                    .build()
            )))
            .active(true)
            .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product created = productService.createProduct(testProduct);

        assertNotNull(created);
        assertEquals("Test Product", created.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_WithNegativePrice_ThrowsException() {

        assertThrows(ProductException.class, () -> {
            productService.createProduct(testProduct);
        });

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void findProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product found = productService.findProductById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        verify(productRepository).findById(1L);
    }

    @Test
    void findProductById_NotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> {
            productService.findProductById(999L);
        });
    }

    @Test
    void findAllProducts_Success() {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Product> result = productService.findAllProducts(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(PageRequest.class));
    }

    @Test
    void createProduct_WithoutVariants_ShouldThrowException() {
        // Given
        Product product = Product.builder()
            .name("Test Product")
            .hasVariants(false)
            .build();
            
        // When & Then
        assertThrows(ProductValidationException.class, 
            () -> productService.createProduct(product));
    }
    
    @Test
    void createProduct_WithVariants_ShouldSucceed() {
        // Given
        Product product = Product.builder()
            .name("Test Product")
            .hasVariants(true)
            .variants(new HashSet<>(Arrays.asList(
                ProductVariant.builder()
                    .sku("TEST-1")
                    .price(BigDecimal.TEN)
                    .stockQuantity(10)
                    .build()
            )))
            .build();
            
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // When
        Product created = productService.createProduct(product);
        
        // Then
        assertNotNull(created);
        assertTrue(created.getHasVariants());
        assertEquals(1, created.getVariants().size());
    }
}