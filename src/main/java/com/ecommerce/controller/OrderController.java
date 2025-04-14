package com.ecommerce.controller;

import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.dto.OrderDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import com.ecommerce.service.interfaces.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management API")
@Validated
@Slf4j
@AllArgsConstructor
public class OrderController {

  private final OrderService orderServiceImpl;
  private final OrderMapper orderMapper;

  @Operation(summary = "Create a new order")
  @Timed(value = "order.creation.time", description = "Order creation time")
  @PostMapping
  @RateLimiter(name = "createOrder")
  public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
      @Valid @RequestBody OrderCreateDTO orderDTO) {

    if (orderDTO.getItems().isEmpty()) {
      throw new IllegalArgumentException("Order must contain at least one item");
    }

    Order created = orderServiceImpl.createOrder(orderDTO);
    OrderDTO orderDto = orderMapper.toDto(created);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                true, orderDto, "Order created successfully", null, LocalDateTime.now()));
  }

  @Operation(summary = "Get all orders", description = "Returns a paginated list of all orders")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
      @Parameter(description = "Page number (starts at 0)") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of elements per page") @RequestParam(defaultValue = "10")
          int size) {

    Page<Order> orders = orderServiceImpl.findAllOrders(PageRequest.of(page, size));
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true, orders, "Orders retrieved successfully", null, LocalDateTime.now()));
  }

  @Operation(summary = "Get orders between two dates")
  @GetMapping("/date-range")
  public ResponseEntity<ApiResponse<Page<Order>>> getOrderBetweenDate(
      @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
      @Parameter(description = "End date") @RequestParam LocalDateTime endDate,
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

    Page<Order> orders =
        orderServiceImpl.findOrdersBetweenDates(startDate, endDate, PageRequest.of(page, size));
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true, orders, "Orders retrieved successfully", null, LocalDateTime.now()));
  }

  @Operation(summary = "Get orders by status")
  @GetMapping("/status/{status}")
  public ResponseEntity<ApiResponse<Page<Order>>> getOrdersByStatus(
      @Parameter(description = "Order status") @PathVariable Status status,
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

    Page<Order> orders = orderServiceImpl.findOrdersByStatus(status, PageRequest.of(page, size));
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true, orders, "Orders retrieved successfully", null, LocalDateTime.now()));
  }

  @Operation(summary = "Get an order", description = "Returns an order by its ID")
  @GetMapping("/{id}")
  @Cacheable(value = "orders", key = "#id")
  @RateLimiter(name = "getOrder")
  public ResponseEntity<ApiResponse<Order>> getOrderByID(
      @Parameter(description = "Order ID") @PathVariable @Positive Long id) {
    log.debug("Searching for order with ID: {}", id);

    Order order = orderServiceImpl.findOrderById(id);
    return ResponseEntity.ok()
        .eTag(String.valueOf(order.getVersion()))
        .body(
            new ApiResponse<>(
                true, order, "Order retrieved successfully", null, LocalDateTime.now()));
  }

  @Operation(summary = "Update order status")
  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
      @Parameter(description = "Order ID") @PathVariable @Positive Long id,
      @Parameter(description = "New status") @RequestParam Status newStatus) {

    Order updatedOrder = orderServiceImpl.updateOrderStatus(id, newStatus);
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true,
                updatedOrder,
                "Order status updated successfully",
                null,
                LocalDateTime.now()));
  }

  @Operation(summary = "Delete an order")
  @DeleteMapping("/{id}")
  @CacheEvict(value = "orders", key = "#id")
  public ResponseEntity<ApiResponse<Void>> deleteOrder(
      @Parameter(description = "Order ID") @PathVariable @Positive Long id) {

    orderServiceImpl.deleteOrder(id);
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(true, null, "Order deleted successfully", null, LocalDateTime.now()));
  }

  @Operation(summary = "Get orders by month")
  @GetMapping("/month")
  public ResponseEntity<ApiResponse<List<Order>>> getOrdersPerMonth(
      @Parameter(description = "Month") @RequestParam @Positive Integer month) {

    List<Order> orders = orderServiceImpl.findOrdersByMonth(month);
    return ResponseEntity.ok()
        .body(
            new ApiResponse<>(
                true, orders, "Orders retrieved successfully", null, LocalDateTime.now()));
  }
}
