package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.ecommerce.model.entity.Product;
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
            .price(new BigDecimal("99.99"))
            .stockInQuantity(10)
            .active(true)
            .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product created = productService.createProduct(testProduct);

        assertNotNull(created);
        assertEquals("Test Product", created.getName());
        assertEquals(new BigDecimal("99.99"), created.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_WithNegativePrice_ThrowsException() {
        testProduct.setPrice(new BigDecimal("-10.00"));

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
}