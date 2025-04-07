package com.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.ecommerce.service.interfaces.ProductImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

  private final ProductImageRepository productImageRepository;
  private final JdbcTemplate jdbcTemplate;
  private final EditSessionServiceImpl editSessionServiceImpl;
  private static final int BATCH_SIZE = 500;

  @Transactional
  public List<ProductImage> upsertProductImagesInBatch(
      List<ProductImageUpsertDTO> productImages, Long productId) {
    List<ProductImage> allResults = new ArrayList<>();
    int batchSize = 50;

    for (int i = 0; i < productImages.size(); i += batchSize) {
      List<ProductImageUpsertDTO> batch =
          productImages.subList(i, Math.min(i + batchSize, productImages.size()));
      log.info("batch: {}", batch);

      String insertSql =
          """
                WITH batch_data (product_id, image_id, filename, session_id) AS (
                    VALUES %s
                ),
                upserted AS (
                    INSERT INTO product_images (product_id, image_id, filename, session_id)
                    SELECT
                        product_id,
                        image_id,
                        filename,
                        session_id
                    FROM batch_data
                    ON CONFLICT (image_id)
                    DO UPDATE SET
                        filename = EXCLUDED.filename,
                        session_id = EXCLUDED.session_id
                    RETURNING id, image_id, filename, session_id
                )
                SELECT * FROM upserted
                """;

      String valuePlaceholders =
          batch.stream().map(v -> "(?, ?, ?, ?)").collect(Collectors.joining(", "));

      log.info("valueplacholders: {}", valuePlaceholders);
      List<Object> paramsList = new ArrayList<>();
      for (ProductImageUpsertDTO v : batch) {

        String imageId = v.getImageId() != null ? v.getImageId() : "";
        String filename = v.getFilename() != null ? v.getFilename() : "";
        String sessionId = v.getSessionId() != null ? v.getSessionId() : "";

        log.info(
            "Image parameters: productId={}, imageId={}, filename={}, session_id={}",
            productId,
            imageId,
            filename,
            sessionId);

        paramsList.add(productId);
        paramsList.add(imageId);
        paramsList.add(filename);
        paramsList.add(sessionId);
      }
      Object[] params = paramsList.toArray(new Object[paramsList.size()]);

      log.info("params: {}", Arrays.toString(params));
      String finalSql = String.format(insertSql, valuePlaceholders);

      log.info("final sql: {}", finalSql);
      List<ProductImage> batchResults =
          jdbcTemplate.query(
              finalSql,
              params,
              (rs, rowNum) -> {
                ProductImage productImage = new ProductImage();
                productImage.setId(rs.getLong("id"));
                productImage.setImageId(rs.getString("image_id"));
                productImage.setFilename(rs.getString("filename"));
                productImage.setSessionId(rs.getString("session_id"));
                return productImage;
              });

      allResults.addAll(batchResults);
    }

    return allResults;
  }


  @Transactional
  public List<ProductImage> upsertProductImages(
      List<ProductImageUpsertDTO> productImages, Long productId) {
    List<ProductImage> allResults = new ArrayList<>();
    int batchSize = 50;

    for (int i = 0; i < productImages.size(); i += batchSize) {
      List<ProductImageUpsertDTO> batch =
          productImages.subList(i, Math.min(i + batchSize, productImages.size()));
      log.info("batch: {}", batch);

      String insertSql =
          """
                WITH batch_data (product_id, image_id, filename, session_id) AS (
                    VALUES %s
                ),
                upserted AS (
                    INSERT INTO product_images (product_id, image_id, filename, session_id)
                    SELECT
                        product_id,
                        image_id,
                        filename,
                        session_id
                    FROM batch_data
                    ON CONFLICT (image_id)
                    DO UPDATE SET
                        filename = EXCLUDED.filename,
                        session_id = EXCLUDED.session_id
                    RETURNING id, image_id, filename, session_id
                )
                SELECT * FROM upserted
                """;

      log.info("INSERT SQL: {}", insertSql);
      String valuePlaceholders =
          batch.stream().map(v -> "(?, ?, ?, ?)").collect(Collectors.joining(", "));

      log.info("valueplacholders: {}", valuePlaceholders);
      List<Object> paramsList = new ArrayList<>();

      for (ProductImageUpsertDTO v : batch) {

        String imageId = v.getImageId() != null ? v.getImageId() : "";
        String filename = v.getFilename() != null ? v.getFilename() : "";
        String sessionId = v.getSessionId() != null ? v.getSessionId() : "";

        log.info(
            "Image parameters: productId={}, imageId={}, filename={}, sessionId={}",
            productId,
            imageId,
            filename,
            sessionId);

        paramsList.add(productId);
        paramsList.add(imageId);
        paramsList.add(filename);
        paramsList.add(sessionId);
      }

      Object[] params = paramsList.toArray(new Object[paramsList.size()]);

      log.info("params: {}", Arrays.toString(params));
      String finalSql = String.format(insertSql, valuePlaceholders);

      log.info("final sql: {}", finalSql);
      List<ProductImage> batchResults =
          jdbcTemplate.query(
              finalSql,
              params,
              (rs, rowNum) -> {
                ProductImage productImage = new ProductImage();
                productImage.setId(rs.getLong("id"));
                productImage.setImageId(rs.getString("image_id"));
                productImage.setFilename(rs.getString("filename"));
                productImage.setSessionId(rs.getString("session_id"));
                return productImage;
              });

      allResults.addAll(batchResults);
    }

    return allResults;
  }

  @Transactional
  public List<ProductImage> upsertProductImagesInSession(
      String sessionId, List<ProductImageUpsertDTO> productImages, Long productId) {
    try {
      productImages.forEach(
          productImage -> {
            productImage.setSessionId(sessionId);
          });

      List<ProductImage> upsertedProductImages = upsertProductImages(productImages, productId);

      List<Long> existingIds =
          productImages.stream()
              .map(ProductImageUpsertDTO::getId)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      Map<Long, ProductImage> existingProductImages =
          productImageRepository.findAllById(existingIds).stream()
              .collect(Collectors.toMap(ProductImage::getId, productImage -> productImage));

      for (ProductImage upsertedProductImage : upsertedProductImages) {
        ProductImage existingProductImage = existingProductImages.get(upsertedProductImage.getId());

        if (existingProductImage != null) {
          editSessionServiceImpl.registerEntityModification(
              sessionId, "PRODUCTIMAGE", upsertedProductImage.getId(), existingProductImage);
        } else {
          editSessionServiceImpl.registerEntityCreation(sessionId, "PRODUCTIMAGE", productId);
        }
      }
      return upsertedProductImages;
    } catch (Exception e) {
      throw new ProductImageUpsertException("Batch error in session: " + e.getMessage());
    }
  }
}
