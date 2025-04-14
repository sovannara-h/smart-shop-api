package com.ecommerce.repository;

import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /** Find orders by status with pagination */
  Page<Order> findByStatus(Status status, Pageable pageable);

  /** Find orders between dates with pagination */
  @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  Page<Order> findBetweenDates(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /** Find order by id with items in a single query */
  @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
  Optional<Order> findByIdWithItems(@Param("id") Long id);

  /** Find orders for a user with items in a single query */
  @Query(
      value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId",
      countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
  Page<Order> findByUserIdWithItems(@Param("userId") Long userId, Pageable pageable);

  /** Use entity graph to load order with items */
  @EntityGraph(attributePaths = {"items"})
  Optional<Order> findWithItemsById(Long id);

  /** Find recent orders with items in a single query */
  @Query(
      "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderDate >= :since ORDER BY o.orderDate DESC")
  Page<Order> findRecentOrdersWithItems(@Param("since") LocalDateTime since, Pageable pageable);
}
