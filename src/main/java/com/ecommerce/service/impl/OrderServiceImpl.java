package com.ecommerce.service.impl;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.interfaces.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Order findOrderById(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("ID must be positive");
    }
    return orderRepository.findById(id).orElseThrow(() -> new OrderException("id"));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findAllOrders(Pageable pageable) {
    return orderRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findOrdersByStatus(Status status, Pageable pageable) {
    log.debug("Searching orders with status: {}", status);
    try {
      return orderRepository.findByStatus(status, pageable);
    } catch (Exception e) {
      log.error("Error searching orders by status", e);
      throw new OrderException("Error searching orders by status: " + e.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public List<Order> findOrdersByMonth(Integer month) {
    if (month < 0) {
      throw new IllegalArgumentException("Month cannot be less than 0");
    }

    if (month > 12) {
      throw new IllegalArgumentException("Month cannot be greater than 12");
    }

    try {
      LocalDateTime startOfMonth =
          LocalDateTime.now()
              .withMonth(month)
              .withDayOfMonth(1)
              .withHour(0)
              .withMinute(0)
              .withSecond(0);

      LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

      return orderRepository
          .findBetweenDates(startOfMonth, endOfMonth, Pageable.unpaged())
          .getContent();

    } catch (Exception e) {
      log.error("Error retrieving orders by month", e);
      throw new OrderException("Error retrieving orders by month: " + e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findOrdersBetweenDates(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    log.debug("Searching orders between {} and {}", startDate, endDate);

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }

    try {
      return orderRepository.findBetweenDates(startDate, endDate, pageable);
    } catch (Exception e) {
      log.error("Error searching orders between dates", e);
      throw new OrderException("Error searching orders between dates: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public Order updateOrderStatus(Long id, Status newStatus) {
    log.debug("Updating order status {} to {}", id, newStatus);

    if (id <= 0) {
      throw new IllegalArgumentException("ID must be positive");
    }

    if (newStatus == null) {
      throw new IllegalArgumentException("New status cannot be null");
    }

    try {
      Order order = findOrderById(id);
      order.setStatus(newStatus);
      return orderRepository.save(order);
    } catch (OrderException e) {
      log.error("Order not found for status update - id: {}", id);
      throw new OrderException("Order not found for status update: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error updating order status", e);
      throw new OrderException("Error updating order status: " + e.getMessage());
    }
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
  public Order createOrder(OrderCreateDTO orderDTO) {
    if (orderDTO == null) {
      throw new IllegalArgumentException("Order data cannot be null");
    }

    if (orderDTO.getEmail() == null || !orderDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new IllegalArgumentException("Invalid email");
    }

    if (orderDTO.getShippingName() == null || orderDTO.getShippingName().trim().isEmpty()) {
      throw new IllegalArgumentException("Shipping name is required");
    }

    if (orderDTO.getShippingAddress() == null || orderDTO.getShippingAddress().trim().isEmpty()) {
      throw new IllegalArgumentException("Shipping address is required");
    }

    if (orderDTO.getShippingPhone() == null
        || !orderDTO.getShippingPhone().matches("^\\+?[0-9]{10,15}$")) {
      throw new IllegalArgumentException("Invalid phone number");
    }

    Order order = new Order();
    order.setCustomerEmail(orderDTO.getEmail());

    if (orderDTO.getUserId() != null) {
      User user =
          userRepository
              .findById(orderDTO.getUserId())
              .orElseThrow(() -> new UserNotFoundException("User not found"));
      order.setUser(user);
    }

    order.setShippingName(orderDTO.getShippingName());
    order.setShippingAddress(orderDTO.getShippingAddress());
    order.setShippingPhone(orderDTO.getShippingPhone());

    return orderRepository.save(order);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteOrder(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("ID must be positive");
    }

    if (!orderRepository.existsById(id)) {
      throw new OrderException("Order not found with ID: " + id);
    }

    try {
      orderRepository.deleteById(id);
    } catch (Exception e) {
      log.error("Error deleting order {}", id, e);
      throw new OrderException("Error deleting order: " + e.getMessage());
    }
  }
}
