package com.ecommerce.service.interfaces;


import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;

public interface OrderService {
    Order createOrder(Order order);
    void deleteOrder(Long id);
    Order updateOrderStatus(Long id, Status newStatus);
    Page<Order> findAllOrders(Pageable pageable);
    Order findOrderById(Long id);
    Page<Order> findOrdersByStatus(Status status ,Pageable pageable);
    Page<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
