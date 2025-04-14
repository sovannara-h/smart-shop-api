package com.ecommerce.repository;

import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.projection.ProductSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @Query(
      "SELECT p FROM Product p JOIN p.categoryProducts cp JOIN cp.category c WHERE c = :category")
  Page<Product> findProductsByCategory(@Param("category") Category category, Pageable pageable);

  /** Optimized query to find a product with all its variants and images */
  @Query(
      """
      SELECT DISTINCT p FROM Product p
      LEFT JOIN FETCH p.variants v
      LEFT JOIN FETCH p.images
      WHERE p.id = :id
      """)
  Optional<Product> findByIdWithDetails(@Param("id") Long id);

  /** Optimized query to find active products with pagination */
  @Query(
      value = """
      SELECT p FROM Product p
      WHERE p.active = true
      """,
      countQuery = "SELECT COUNT(p) FROM Product p WHERE p.active = true")
  Page<Product> findAllActiveProducts(Pageable pageable);

  /** Optimized query with entity graph */
  @EntityGraph(attributePaths = {"variants", "images", "categoryProducts"})
  Optional<Product> findWithGraphById(Long id);

  /** Optimized query to find products by category with fetch joins */
  @Query(
      """
      SELECT DISTINCT p FROM Product p
      JOIN FETCH p.categoryProducts cp
      JOIN FETCH cp.category c
      LEFT JOIN FETCH p.variants
      WHERE c.id = :categoryId AND p.active = true
      """)
  List<Product> findActiveByCategoryIdWithDetails(@Param("categoryId") Long categoryId);

  // Ajouter cette méthode pour le chargement paginé avec EntityGraph
  @EntityGraph(attributePaths = {"images"})
  @Query(value = "SELECT p FROM Product p", countQuery = "SELECT COUNT(p) FROM Product p")
  Page<Product> findAllWithBasicDetails(Pageable pageable);

  // Ajouter cette méthode pour optimiser le chargement par catégorie
  @EntityGraph(attributePaths = {"images", "variants"})
  @Query(
      value =
          "SELECT p FROM Product p JOIN p.categoryProducts cp JOIN cp.category c WHERE c = :category",
      countQuery =
          "SELECT COUNT(p) FROM Product p JOIN p.categoryProducts cp JOIN cp.category c WHERE c = :category")
  Page<Product> findProductsByCategoryWithDetails(
      @Param("category") Category category, Pageable pageable);

  // Méthode optimisée pour récupérer seulement les informations nécessaires pour une liste
  @Query(
      value =
          """
      SELECT p.id as id, p.name as name, p.description as description,
             p.rating as rating, p.number_of_reviews as numberOfReviews,
             p.active as active, p.has_variants as hasVariants,
             (SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id LIMIT 1) as mainImageUrl
      FROM products p
      WHERE p.active = true
      """,
      nativeQuery = true,
      countQuery = "SELECT COUNT(*) FROM products WHERE active = true")
  Page<ProductSummary> findAllActiveProductsOptimized(Pageable pageable);

  @Query(value = "SELECT COUNT(*) FROM products", nativeQuery = true)
  long countProducts();
}
