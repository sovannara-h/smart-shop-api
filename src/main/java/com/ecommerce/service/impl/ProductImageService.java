package com.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ProductImageUpsertException;
import com.ecommerce.model.dto.ProductImageUpsertDTO;
import com.ecommerce.model.entity.ProductImage;
import com.ecommerce.repository.ProductImageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EditSessionService editSessionService;
    private static final int BATCH_SIZE = 500;

    // ... existing code ...

@Transactional
public List<ProductImage> upsertProductImagesInBatch(List<ProductImageUpsertDTO> productImages, Long productId) {
    List<ProductImage> allResults = new ArrayList<>();
    int batchSize = 50;

    for (int i = 0; i < productImages.size(); i += batchSize) {
        List<ProductImageUpsertDTO> batch = productImages.subList(i, Math.min(i + batchSize, productImages.size()));
        log.info("batch: {}", batch);

        String insertSql = """
            WITH batch_data (product_id, image_id, filename, sessions) AS (
                VALUES %s
            ),
            upserted AS (
                INSERT INTO product_images (product_id, image_id, filename, sessions)
                SELECT
                    product_id,
                    image_id,
                    filename,
                    CAST(sessions AS jsonb)
                FROM batch_data
                ON CONFLICT (image_id)
                DO UPDATE SET
                    filename = EXCLUDED.filename,
                    sessions = EXCLUDED.sessions
                RETURNING id, image_id, filename, sessions
            )
            SELECT * FROM upserted
            """;

        String valuePlaceholders = batch.stream()
            .map(v -> "(?, ?, ?, ?)")
            .collect(Collectors.joining(", "));

        log.info("valueplacholders: {}", valuePlaceholders);
        List<Object> paramsList = new ArrayList<>();
        for (ProductImageUpsertDTO v : batch) {
            try {
                // Vérification des valeurs nulles
                String imageId = v.getImageId() != null ? v.getImageId() : "";
                String filename = v.getFilename() != null ? v.getFilename() : "";
                String sessionsJson = v.getSessions() != null 
                    ? new ObjectMapper().writeValueAsString(v.getSessions()) 
                    : "{}";
                    
                log.info("Paramètres d'image: productId={}, imageId={}, filename={}, sessions={}", 
                         productId, imageId, filename, sessionsJson);
                
                // Ajouter tous les paramètres à la liste
                paramsList.add(productId);
                paramsList.add(imageId);
                paramsList.add(filename);
                paramsList.add(sessionsJson);
            } catch (JsonProcessingException e) {
                log.error("Erreur lors de la conversion des sessions en JSON", e);
                throw new RuntimeException("Erreur de conversion JSON", e);
            }
        }
        Object[] params = paramsList.toArray(new Object[paramsList.size()]);

        log.info("params: {}", Arrays.toString(params));
        String finalSql = String.format(insertSql, valuePlaceholders);

        log.info("final sql: {}", finalSql);
        List<ProductImage> batchResults = jdbcTemplate.query(
            finalSql,
            params,
            (rs, rowNum) -> {
                ProductImage productImage = new ProductImage();
                productImage.setId(rs.getLong("id"));
                productImage.setImageId(rs.getString("image_id"));
                productImage.setFilename(rs.getString("filename"));
                try {
                    productImage.setSessions(new ObjectMapper().readValue(
                        rs.getString("sessions"), 
                        new TypeReference<Map<String, Object>>() {}
                    ));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Erreur de désérialisation JSON", e);
                }
                return productImage;
            }
        );

        allResults.addAll(batchResults);
    }

    return allResults;
}

    public List<ProductImage> upsertProductImages(List<ProductImageUpsertDTO> productImages, Long productId) {
        List<ProductImage> allResults = new ArrayList<>();
        int batchSize = 50;
    
        for (int i = 0; i < productImages.size(); i += batchSize) {
            List<ProductImageUpsertDTO> batch = productImages.subList(i, Math.min(i + batchSize, productImages.size()));
            log.info("batch: {}", batch);
    
            String insertSql = """
                WITH batch_data (product_id, image_id, filename, sessions) AS (
                    VALUES %s
                ),
                upserted AS (
                    INSERT INTO product_images (product_id, image_id, filename, sessions)
                    SELECT
                        product_id,
                        image_id,
                        filename,
                        CAST(sessions AS jsonb)
                    FROM batch_data
                    ON CONFLICT (image_id)
                    DO UPDATE SET
                        filename = EXCLUDED.filename,
                        sessions = EXCLUDED.sessions
                    RETURNING id, image_id, filename, sessions
                )
                SELECT * FROM upserted
                """;
    
            String valuePlaceholders = batch.stream()
                .map(v -> "(?, ?, ?, ?)")
                .collect(Collectors.joining(", "));
    
            log.info("valueplacholders: {}", valuePlaceholders);
            List<Object> paramsList = new ArrayList<>();
            for (ProductImageUpsertDTO v : batch) {
                try {
                    // Vérification des valeurs nulles
                    String imageId = v.getImageId() != null ? v.getImageId() : "";
                    String filename = v.getFilename() != null ? v.getFilename() : "";
                    String sessionsJson = v.getSessions() != null 
                        ? new ObjectMapper().writeValueAsString(v.getSessions()) 
                        : "{}";
                        
                    log.info("Paramètres d'image: productId={}, imageId={}, filename={}, sessions={}", 
                             productId, imageId, filename, sessionsJson);
                    
                    // Ajouter tous les paramètres à la liste
                    paramsList.add(productId);
                    paramsList.add(imageId);
                    paramsList.add(filename);
                    paramsList.add(sessionsJson);
                } catch (JsonProcessingException e) {
                    log.error("Erreur lors de la conversion des sessions en JSON", e);
                    throw new RuntimeException("Erreur de conversion JSON", e);
                }
            }
            Object[] params = paramsList.toArray(new Object[paramsList.size()]);
    
            log.info("params: {}", Arrays.toString(params));
            String finalSql = String.format(insertSql, valuePlaceholders);
    
            log.info("final sql: {}", finalSql);
            List<ProductImage> batchResults = jdbcTemplate.query(
                finalSql,
                params,
                (rs, rowNum) -> {
                    ProductImage productImage = new ProductImage();
                    productImage.setId(rs.getLong("id"));
                    productImage.setImageId(rs.getString("image_id"));
                    productImage.setFilename(rs.getString("filename"));
                    try {
                        productImage.setSessions(new ObjectMapper().readValue(
                            rs.getString("sessions"), 
                            new TypeReference<Map<String, Object>>() {}
                        ));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Erreur de désérialisation JSON", e);
                    }
                    return productImage;
                }
            );
    
            allResults.addAll(batchResults);
        }
    
        return allResults;
    }
    
    @Transactional
    public List<ProductImage> upsertProductImagesInSession(String sessionId, List<ProductImageUpsertDTO> productImages, Long productId) {

        try {
            productImages.forEach(productImage -> {
                Map<String, Object> sessions = new HashMap<>();
                sessions.put("session", Map.of(
                    "sessionId", sessionId,
                    "status", "pending"
                ));
                log.info("SESIONN, {}", sessions);
                productImage.setSessions(sessions);
            });

            List<ProductImage> upsertedProductImages = upsertProductImages(productImages, productId);

            List<Long> existingIds = productImages.stream()
                .map(ProductImageUpsertDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            Map<Long, ProductImage> existingProductImages = productImageRepository.findAllById(existingIds)
                .stream()
                .collect(Collectors.toMap(ProductImage::getId, productImage -> productImage));

            for(ProductImage upsertedProductImage: upsertedProductImages) {
                ProductImage existingProductImage = existingProductImages.get(upsertedProductImage.getId());

                if(existingProductImage != null) {
                    editSessionService.registerEntityModification(
                        sessionId,
                        "PRODUCTIMAGE",
                        upsertedProductImage.getId(),
                        existingProductImage
                    );
                } else {
                    editSessionService.registerEntityCreation(
                        sessionId, 
                        "PRODUCTIMAGE", 
                        productId
                    );
                }
            }
            return upsertedProductImages;
        } catch (Exception e) {
            throw new ProductImageUpsertException("Erreur batch en session: " + e.getMessage());
        }
    }
}
