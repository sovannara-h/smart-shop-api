package com.ecommerce.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.exception.ProductException;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.interfaces.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        try {
            Product savedProduct = productRepository.save(product);
            return savedProduct;
        } catch (Exception e) {
            throw new ProductException(e.getMessage());
        }
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        try {
            Product existingProduct = findProductById(id);
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setCategories(product.getCategories());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setRating(product.getRating());
            existingProduct.setNumberOfReviews(product.getNumberOfReviews());
            existingProduct.setActive(product.getActive());
            existingProduct.setAttributes(product.getAttributes());
            existingProduct.setInteractions(product.getInteractions());
            existingProduct.setStockInQuantity(product.getStockInQuantity());
            
            return productRepository.save(existingProduct);
        } catch (Exception e) {
            throw new ProductException(e.getMessage());
        }
    }

    @Override
    public void deleteProduct(Long id) {
        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            throw new ProductException(e.getMessage());
        }
    }

    @Override
    public Product findProductById(Long id) {
        if(id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        return productRepository.findById(id).orElseThrow(() -> new ProductException("id"));
    }

    @Override
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findProductsByCategory(Category category, Pageable pageable) {
        return findProductsByCategory(category, pageable);
    }

    
}
