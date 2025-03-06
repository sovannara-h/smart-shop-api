package com.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.dto.ProductVariantCreateDTO;
import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductVariantServiceImpl {
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;

    private String generateSku(Product product, Map<String, String> attributes) {
        String baseCode = product.getName().substring(0, Math.min(3, product.getName().length())).toUpperCase();
        String variantCode = attributes.values().stream()
            .map(v -> v.substring(0, Math.min(2, v.length())).toUpperCase())
            .collect(Collectors.joining("-"));
        return String.format("%s-%s-%d", baseCode, variantCode, System.currentTimeMillis() % 10000);
    }

    private List<Map<String, String>> generateCombinations(Map<String, List<String>> attributes) {
        List<Map<String, String>> combinations = new ArrayList<>();
        
        if (attributes.isEmpty()) {
            return combinations;
        }

        Map<String, String> initial = new HashMap<>();

        for(Map.Entry<String, List<String>> entry: attributes.entrySet()) {
            String attributeName = entry.getKey();
            List<String> attributeValues = entry.getValue();

            List<Map<String, String>> newCombinations = new ArrayList<>();

            for(Map<String, String> combination: combinations) {
                for(String value: attributeValues) {
                    Map<String, String> newCombination = new HashMap<>(combination);
                    newCombination.put(attributeName, value);
                    newCombinations.add(newCombination);
                }
            }
            combinations = newCombinations;
        }

        return combinations;
    }

    public List<ProductVariant> generateAndSaveVariants(ProductVariantCreateDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé"));
        
        List<Map<String, String>> combinations = generateCombinations(dto.getAttributes());

        List<ProductVariant> variants = combinations.stream()
            .map(combination -> createVariant(product, combination, dto))
            .collect(Collectors.toList());

        product.setHasVariants(true);
        product.getVariants().addAll(variants);
        productRepository.save(product);
        
        return variants;
    }

    private ProductVariant createVariant(Product product, Map<String, String> combination, ProductVariantCreateDTO dto) {

        String sku = generateSku(product, combination);
        
        Set<AttributeValue> attributeValues = combination.entrySet().stream()
            .map(entry -> {
                Attribute attribute = attributeRepository.findByName(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Attribut non trouvé: " + entry.getKey()));
                
                return AttributeValue.builder()
                    .attribute(attribute)
                    .value(entry.getValue())
                    .build();
            })
            .collect(Collectors.toSet());
        
        // Construire et retourner la variante
        return ProductVariant.builder()
            .product(product)
            .sku(sku)
            .price(dto.getBasePrice())
            .stockQuantity(dto.getBaseStock())
            .attributeValues(attributeValues)
            .build();
    }
}
