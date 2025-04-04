package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
  Order createOrder(OrderCreateDTO orderDTO);

  void deleteOrder(Long id);

  Order updateOrderStatus(Long id, Status newStatus);

  Page<Order> findAllOrders(Pageable pageable);

  Order findOrderById(Long id);

  Page<Order> findOrdersByStatus(Status status, Pageable pageable);

  Page<Order> findOrdersBetweenDates(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
