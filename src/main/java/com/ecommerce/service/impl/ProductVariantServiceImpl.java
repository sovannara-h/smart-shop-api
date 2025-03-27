package com.ecommerce.service.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.exception.VariantCreationException;
import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.ProductVariantGenerateCombinationDTO;
import com.ecommerce.model.dto.VariantCreateDTO;
import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.model.entity.ProductVariantAttributeValue;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.repository.AttributeValueRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantAttributeValueRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final JdbcTemplate jdbcTemplate;
    private static final int BATCH_SIZE = 500;
    private final EditSessionService editSessionService;
    private final ProductVariantAttributeValueRepository productVariantAttributeValueRepository;
    
    private String generateSku(Product product, Map<String, String> combination) {
        String baseSkuPart = product.getId().toString();
        String variantPart = combination.values().stream()
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining("-"));
        return String.format("%s-%s", baseSkuPart, variantPart);
    }

    private List<ProductVariant> findAllByProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("L'ID du produit ne peut pas être null");
        }
        try {
            return productVariantRepository.findAllByProductId(productId);
        } catch (Exception e) {
            log.error("Errreur lors de la recherche des variantes pour le produit {}: {}", productId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des variantes du produit", e);
        }
    }


    private List<Map<String, String>> generateCombinations(Map<String, List<Map<String, Object>>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }
    
        List<Map<String, String>> combinations = new ArrayList<>();
        combinations.add(new HashMap<>());
    
        for (Map.Entry<String, List<Map<String, Object>>> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            List<Map<String, Object>> values = entry.getValue();
            
            if (values == null || values.isEmpty()) {
                continue;
            }
    
            List<Map<String, String>> newCombinations = new ArrayList<>();
    
            for (Map<String, String> combination : combinations) {
                for (Map<String, Object> valueMap : values) {
                    Map<String, String> newCombination = new HashMap<>(combination);
                    newCombination.put(attributeName, valueMap.get("value").toString());
                    newCombination.put(attributeName + "_id", valueMap.get("id").toString());
                    newCombinations.add(newCombination);
                }
            }
    
            combinations = newCombinations;
        }
    
        return combinations;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> generateVariants(ProductVariantGenerateCombinationDTO dto) {
        log.info("ATTRIBU : {}", dto.getAttributes());

        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé: " + dto.getProductId()));

        List<AttributeDTO> attributesWithValues = attributeService.findAttributesWithValuesByIds(dto.getAttributes());
        log.info("Attributs récupérés: {}", attributesWithValues);
        
        Map<String, List<Map<String, Object>>> attributesMap = attributesWithValues.stream()
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

        List<Map<String, Object>> formattedCombinations = new ArrayList<>();
        
        combinations.forEach(combination -> {
            Map<String, Object> formattedCombination = new LinkedHashMap<>();
            
            String sku = String.format("%d-%s", product.getId(),
                combination.entrySet().stream()
                    .filter(e -> !e.getKey().endsWith("_id"))
                    .map(Map.Entry::getValue)
                    .sorted()
                    .collect(Collectors.joining("-")));
            ProductVariant foundVariant = variantsBySku.get(sku);
            
            if (foundVariant != null) {
                formattedCombination.put("id", foundVariant.getId());
            }

            formattedCombination.put("sku", sku);
            
            List<Map<String, Object>> attributeValuesList = new ArrayList<>();
            
            attributesMap.keySet().forEach(attrName -> {
                Map<String, Object> attributeValue = new HashMap<>();
                attributeValue.put("id", Long.parseLong(combination.get(attrName + "_id")));
                attributeValue.put("name", attrName);
                attributeValue.put("value", combination.get(attrName));
                attributeValue.put("attribute_id", attributesWithValues.stream()
                    .filter(attr -> attr.getName().equals(attrName))
                    .findFirst()
                    .map(AttributeDTO::getId)
                    .orElse(null));
                attributeValuesList.add(attributeValue);
            });
            
            formattedCombination.put("attributeValues", attributeValuesList);
            formattedCombination.put("price", foundVariant != null ? foundVariant.getPrice() : dto.getBasePrice());
            formattedCombination.put("stockQuantity", foundVariant != null ? foundVariant.getStockQuantity() : dto.getBaseStock());

            formattedCombinations.add(formattedCombination);
        });

        return formattedCombinations;
    }


    private ProductVariant createVariant(Product product, Map<String, String> combination, ProductVariantGenerateCombinationDTO dto) {
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
                    existing.getVariantAttributeValues().clear();
                    existing.getVariantAttributeValues().addAll(attributeValues.stream()
                        .map(av -> ProductVariantAttributeValue.builder()
                            .attributeValue(av)
                            .build())
                        .collect(Collectors.toSet()));
                    return productVariantRepository.save(existing);
                })
                .orElseGet(() -> {
                    ProductVariant newVariant = ProductVariant.builder()
                        .product(product)
                        .sku(sku)
                        .price(dto.getBasePrice())
                        .stockQuantity(dto.getBaseStock())
                        .variantAttributeValues(attributeValues.stream()
                            .map(av -> ProductVariantAttributeValue.builder()
                                .attributeValue(av)
                                .build())
                            .collect(Collectors.toSet()))
                        .build();
                    ProductVariant saved = productVariantRepository.save(newVariant);
                    saved.getVariantAttributeValues().forEach(pvav -> pvav.setProductVariant(saved));
                    return saved;
                });
            log.info("CREATE V: ", variant);
            return variant;
        } catch (Exception e) {
            log.error("Erreur lors de la création/mise à jour de la variante - Produit: {}, Combinaison: {}, Erreur: {}", 
                product.getId(), combination, e.getMessage());
            throw new VariantCreationException("Erreur lors de la création de la variante: " + e.getMessage());
        }
    }

    public List<ProductVariant> batchUpsert(List<VariantCreateDTO> variants, Long productId) {
        log.info("VARIANTS UPSERT: {}", variants);
        if(variants.isEmpty()) {
            return Collections.emptyList();
        }
    

        List<ProductVariant> allResults = new ArrayList<>();
    
        for (int i = 0; i < variants.size(); i += BATCH_SIZE) {
            List<VariantCreateDTO> batch = variants.subList(i, Math.min(i + BATCH_SIZE, variants.size()));
    
            String insertSql = """
                WITH batch_data (product_id, sku, price, stock_quantity, sessions) AS (
                    VALUES %s
                ),
                upserted AS (
                    INSERT INTO product_variants (product_id, sku, price, stock_quantity, sessions)
                    SELECT 
                        product_id,
                        sku,
                        price,
                        stock_quantity,
                        sessions::jsonb
                    FROM batch_data
                    ON CONFLICT (sku)
                    DO UPDATE SET
                        price = EXCLUDED.price,
                        stock_quantity = EXCLUDED.stock_quantity,
                        sessions = EXCLUDED.sessions
                    RETURNING id, sku, price, stock_quantity, sessions
                )
                SELECT * FROM upserted
                """;
    
            String valuePlaceholders = batch.stream()
                .map(v -> "(?, ?, ?, ?, ?::json)")
                .collect(Collectors.joining(", "));
    
            Object[] params = batch.stream()
                .flatMap(v -> {
                    try {
                        return Stream.of(
                            productId,
                            v.getSku(),
                            v.getPrice(),
                            v.getStockQuantity(),
                            new ObjectMapper().writeValueAsString(v.getSessions())
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Erreur de conversion JSON", e);
                    }
                })
                .toArray();
    
            String finalSql = String.format(insertSql, valuePlaceholders);
    
            List<ProductVariant> batchResults = jdbcTemplate.query(
                finalSql,
                params,
                (rs, rowNum) -> {
                    ProductVariant variant = new ProductVariant();
                    variant.setId(rs.getLong("id"));
                    variant.setSku(rs.getString("sku"));
                    variant.setPrice(rs.getBigDecimal("price"));
                    variant.setStockQuantity(rs.getInt("stock_quantity"));
                    try {
                        variant.setSessions(new ObjectMapper().readValue(
                            rs.getString("sessions"), 
                            new TypeReference<Map<String, Object>>() {}
                        ));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Erreur de désérialisation JSON", e);
                    }
                    return variant;
                }
            );
    
            allResults.addAll(batchResults);
        }
    
        return allResults;
    }
    

    public void batchUpsertAttributeValues(Long variantId, List<Long> attributeValues) {
        String sql = """
            INSERT INTO product_variant_attribute_values 
            (product_variant_id, attribute_value_id, created_at, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (product_variant_id, attribute_value_id) 
            DO UPDATE SET
                updated_at = CURRENT_TIMESTAMP
            """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long value = attributeValues.get(i);
                ps.setLong(1, variantId);
                ps.setLong(2, value);
            }

            @Override
            public int getBatchSize() {
                return attributeValues.size();
            }
        });
    }

    @Transactional
    public List<ProductVariant> batchUpsertVariants(List<VariantCreateDTO> variants, Long productId) {
        try {

            List<ProductVariant> productVariants = batchUpsert(variants, productId);
            
            for (VariantCreateDTO variant : variants) {
                if (!variant.getAttributeValues().isEmpty()) {
                    log.info("attribbuteVAL: {}", variant.getAttributeValues());
                    // batchUpsertAttributeValues(
                    //     variant.getId(), 
                    //     variant.getAttributeValues()
                    // );
                }
            }

            return productVariants;
        } catch (Exception e) {
            log.error("Erreur lors du batch upsert des variants", e);
            throw new VariantCreationException("Erreur batch: " + e.getMessage());
        }
    }

    @Transactional
    public List<ProductVariant> batchUpsertVariantsInSession(String sessionId, List<VariantCreateDTO> productVariants, Long productId) {
        try {
        List<Long> existingIds = productVariants.stream()
        .map(VariantCreateDTO::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());


    Map<Long, ProductVariant> existingVariantsMap = productVariantRepository.findAllById(existingIds)
        .stream()
        .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));

    productVariants.forEach(variant -> {
        Map<String, Object> sessions = new HashMap<>();
        sessions.put("session", Map.of(
            "sessionId", sessionId,
            "status", "pending"
        ));
        variant.setSessions(sessions);
    });

    List<ProductVariant> upsertedVariants = batchUpsert(productVariants, productId);

    for (int i = 0; i < upsertedVariants.size(); i++) {
        ProductVariant variant = upsertedVariants.get(i);
        VariantCreateDTO dto = productVariants.get(i);
        
        if (!dto.getAttributeValues().isEmpty()) {
            batchUpsertAttributeValues(variant.getId(), dto.getAttributeValues());
        }
    }


    for (ProductVariant upsertedVariant : upsertedVariants) {
        ProductVariant existingVariant = existingVariantsMap.get(upsertedVariant.getId());
        
        if (existingVariant != null) {
            editSessionService.registerEntityModification(
                sessionId,
                "PRODUCTVARIANT",
                upsertedVariant.getId(),
                existingVariant
            );
        } else {
            editSessionService.registerEntityCreation(
                sessionId,
                "PRODUCTVARIANT",
                upsertedVariant.getId()
            );
        }
    }

    return upsertedVariants;
        } catch (Exception e) {
            log.error("Erreur lors du batch upsert des variants en session", e);
            throw new VariantCreationException("Erreur batch en session: " + e.getMessage());
        }
    }   
}


