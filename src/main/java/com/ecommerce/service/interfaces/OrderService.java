package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Service for order management */
public interface OrderService {
  /**
   * Creates a new order
   *
   * @param orderDTO order creation data
   * @return the created order
   */
  Order createOrder(OrderCreateDTO orderDTO);

  /**
   * Deletes an order
   *
   * @param id order identifier
   */
  void deleteOrder(Long id);

  /**
   * Updates the status of an order
   *
   * @param id order identifier
   * @param newStatus new status value
   * @return the updated order
   */
  Order updateOrderStatus(Long id, Status newStatus);

  /**
   * Retrieves all orders with pagination
   *
   * @param pageable pagination information
   * @return paginated list of orders
   */
  Page<Order> findAllOrders(Pageable pageable);

  /**
   * Finds an order by its ID
   *
   * @param id order identifier
   * @return the found order
   */
  Order findOrderById(Long id);

  /**
   * Finds orders by status with pagination
   *
   * @param status order status to filter by
   * @param pageable pagination information
   * @return paginated list of orders with the specified status
   */
  Page<Order> findOrdersByStatus(Status status, Pageable pageable);

  /**
   * Finds orders between two dates with pagination
   *
   * @param startDate start date for the search range
   * @param endDate end date for the search range
   * @param pageable pagination information
   * @return paginated list of orders within the date range
   */
  Page<Order> findOrdersBetweenDates(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * Finds orders by month
   *
   * @param month month number (1-12)
   * @return list of orders placed in the specified month
   * @throws IllegalArgumentException if the month is not valid (must be between 1 and 12)
   * @throws OrderException if an error occurs while retrieving orders
   */
  List<Order> findOrdersByMonth(Integer month);
}
