package com.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.exception.VariantCreationException;
import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.ProductVariantCreateDTO;
import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.repository.AttributeValueRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductVariantServiceImpl {
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeServiceImpl attributeService;

    private String generateSku(Product product, Map<String, String> combination) {
        String baseSkuPart = product.getId().toString();
        String variantPart = combination.values().stream()
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining("-"));
        return String.format("%s-%s", baseSkuPart, variantPart);
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> generateVariants(ProductVariantCreateDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé: " + dto.getProductId()));

        List<AttributeDTO> attributesWithValues = attributeService.findAttributesWithValuesByIds(dto.getAttributes());
        log.info("Attributs récupérés: {}", attributesWithValues);
        
        Map<String, List<String>> attributesMap = attributesWithValues.stream()
            .collect(Collectors.toMap(
                AttributeDTO::getName,
                AttributeDTO::getValues
            ));
        log.info("Map d'attributs: {}", attributesMap);
        
        List<Map<String, String>> combinations = generateCombinations(attributesMap);
        log.info("Combinaisons générées: {}", combinations);

        List<ProductVariant> existingVariants = findAllByProductId(product.getId());

        Map<String, ProductVariant> variantsBySku = existingVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getSku, v -> v));

        combinations.forEach(combination -> {
            String sku = generateSku(product, combination);
            ProductVariant foundVariant = variantsBySku.get(sku);
            
            Map<String, String> orderedCombination = new LinkedHashMap<>();            

            orderedCombination.put("sku", sku);
            
            combination.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("sku") 
                    && !entry.getKey().equals("stockQuantity") 
                    && !entry.getKey().equals("price"))
                .forEach(entry -> orderedCombination.put(entry.getKey(), entry.getValue()));
            
            orderedCombination.put("stockQuantity", 
                foundVariant != null ? foundVariant.getStockQuantity().toString() : dto.getBaseStock().toString());
            
            orderedCombination.put("price", 
                foundVariant != null ? foundVariant.getPrice().toString() : dto.getBasePrice().toString());
            
            combinations.set(combinations.indexOf(combination), orderedCombination);
        });

        return combinations;
    }

    // public List<ProductVariant> generateAndSaveVariants(ProductVariantCreateDTO dto) {
    //     log.info("Génération des variantes pour le produit {} avec les attributs: {}", 
    //         dto.getProductId(), dto.getAttributes());
            
    //     Product product = productRepository.findById(dto.getProductId())
    //         .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé: " + dto.getProductId()));
        
    //     // Génération des combinaisons
    //     List<Map<String, String>> combinations = generateCombinations(dto.getAttributes());
    //     log.info("Nombre de combinaisons générées: {}", combinations.size(), combinations);
        
    //     if (combinations.isEmpty()) {
    //         log.warn("Aucune combinaison générée pour les attributs: {}", dto.getAttributes());
    //         return Collections.emptyList();
    //     }

    //     // Création des variantes
    //     List<ProductVariant> variants = combinations.stream()
    //         .map(combination -> {
    //             try {
    //                 return createVariant(product, combination, dto);
    //             } catch (Exception e) {
    //                 log.error("Erreur lors de la création d'une variante: {}", e.getMessage());
    //                 return null;
    //             }
    //         })
    //         .filter(Objects::nonNull)
    //         .collect(Collectors.toList());

    //     log.info("VAR: {}", variants.stream()
    //         .map(v -> String.format("ID: %d, SKU: %s, AttributeValues: %s", 
    //             v.getId(), 
    //             v.getSku(), 
    //             v.getAttributeValues().stream()
    //                 .map(av -> av.getValue())
    //                 .collect(Collectors.toList())
    //         ))
    //         .collect(Collectors.toList()));
        
    //     // Sauvegarde des variantes
    //     if (!variants.isEmpty()) {
    //         product.setHasVariants(true);
    //         product.getVariants().addAll(variants);
    //         productRepository.save(product);
    //     }
        
    //     return variants;
    // }

    private List<ProductVariant> findAllByProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("L'ID du produit ne peut pas être null");
        }
        try {
            return productVariantRepository.findAllByProductId(productId);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche des variantes pour le produit {}: {}", productId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des variantes du produit", e);
        }
    }


    private List<Map<String, String>> generateCombinations(Map<String, List<String>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            log.warn("Aucun attribut fourni pour la génération des combinaisons");
            return Collections.emptyList();
        }

        List<Map<String, String>> combinations = new ArrayList<>();
        combinations.add(new HashMap<>());

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            List<String> values = entry.getValue();
            
            log.info("Traitement de l'attribut {} avec les valeurs: {}", attributeName, values);

            if (values == null || values.isEmpty()) {
                log.warn("Aucune valeur pour l'attribut: {}", attributeName);
                continue;
            }

            List<Map<String, String>> newCombinations = new ArrayList<>();

            for (Map<String, String> combination : combinations) {
                for (String value : values) {
                    Map<String, String> newCombination = new HashMap<>(combination);
                    newCombination.put(attributeName, value);
                    // String sku = generateSkuWithoutProduct(newCombination);
                    // newCombination.put("sku", sku);
                    newCombinations.add(newCombination);
                }
            }

            combinations = newCombinations;
        }

        log.info("Nombre total de combinaisons générées: {}", combinations.size());
        return combinations;
    }

    private ProductVariant createVariant(Product product, Map<String, String> combination, ProductVariantCreateDTO dto) {
        try {
            String sku = generateSku(product, combination);
            
            Set<AttributeValue> attributeValues = combination.entrySet().stream()
                .map(entry -> {
                    Attribute attribute = attributeRepository.findByName(entry.getKey())
                        .orElseGet(() -> {
                            Attribute newAttribute = new Attribute();
                            newAttribute.setName(entry.getKey());
                            return attributeRepository.save(newAttribute);
                        });
                    
                    return attributeValueRepository.findByAttributeAndValue(attribute, entry.getValue())
                        .orElseGet(() -> {
                            AttributeValue newValue = AttributeValue.builder()
                                .attribute(attribute)
                                .value(entry.getValue())
                                .build();
                            return attributeValueRepository.save(newValue);
                        });
                })
                .collect(Collectors.toSet());
            
            ProductVariant variant = productVariantRepository.findBySku(sku)
                .map(existing -> {
                    existing.setPrice(dto.getBasePrice());
                    existing.setStockQuantity(dto.getBaseStock());
                    existing.getAttributeValues().clear();
                    existing.getAttributeValues().addAll(attributeValues);
                    return productVariantRepository.save(existing);
                })
                .orElseGet(() -> {
                    ProductVariant newVariant = ProductVariant.builder()
                        .product(product)
                        .sku(sku)
                        .price(dto.getBasePrice())
                        .stockQuantity(dto.getBaseStock())
                        .attributeValues(attributeValues)
                        .build();
                    return productVariantRepository.save(newVariant);
                });
            log.info("CREATE V: ", variant);
            return variant;
        } catch (Exception e) {
            log.error("Erreur lors de la création/mise à jour de la variante - Produit: {}, Combinaison: {}, Erreur: {}", 
                product.getId(), combination, e.getMessage());
            throw new VariantCreationException("Erreur lors de la création de la variante: " + e.getMessage());
        }
    }
}
