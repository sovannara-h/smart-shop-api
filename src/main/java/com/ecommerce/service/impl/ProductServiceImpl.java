package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ProductConcurrencyException;
import com.ecommerce.exception.ProductException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.exception.ProductValidationException;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.interfaces.ProductService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private void _validateProduct(Product product) {
        List<String> errors = new ArrayList<>();

        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Le prix ne peut pas être négatif");
        }
        if (product.getStockInQuantity() < 0) {
            errors.add("Le stock ne peut pas être négatif");
        }
        
        if (!errors.isEmpty()) {
            throw new ProductValidationException(String.join(", ", errors));
        }
    }

    private void _updateProductFields(Product existing, Product updated) {
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCategories(updated.getCategories());
        existing.setPrice(updated.getPrice());
        existing.setStockInQuantity(updated.getStockInQuantity());
        existing.setActive(updated.getActive());
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        log.debug("Création d'un nouveau produit : {}", product.getName());
        _validateProduct(product);
        
        try {
            return productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            log.error("Erreur d'intégrité des données lors de la création du produit", e);
            throw new ProductException("Un produit avec ces informations existe déjà");
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création du produit", e);
            throw new ProductException("Erreur lors de la création du produit: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        log.debug("Mise à jour du produit avec l'ID : {}", id);
        _validateProduct(product);
        
        try {
            Product existingProduct = findProductById(id);
            
            if (!existingProduct.getVersion().equals(product.getVersion())) {
                throw new ProductConcurrencyException("Le produit a été modifié par un autre utilisateur");
            }
            
            _updateProductFields(existingProduct, product);
            return productRepository.save(existingProduct);
        } catch (ProductNotFoundException | ProductConcurrencyException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du produit {}", id, e);
            throw new ProductException("Erreur lors de la mise à jour: " + e.getMessage());
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
