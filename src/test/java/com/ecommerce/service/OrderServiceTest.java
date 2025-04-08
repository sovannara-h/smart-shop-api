package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.dto.OrderItemDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.impl.OrderServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private OrderServiceImpl orderService;

  private Order testOrder;
  private OrderCreateDTO orderCreateDTO;
  private User testUser;
  private List<OrderItemDTO> orderItems;
  private PageRequest pageRequest;

  @BeforeEach
  void setUp() {
    pageRequest = PageRequest.of(0, 10);

    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");

    testOrder =
        Order.builder()
            .id(1L)
            .status(Status.PENDING)
            .customerEmail("test@example.com")
            .orderDate(LocalDateTime.now())
            .shippingName("John Doe")
            .shippingAddress("123 Main St")
            .shippingPhone("+1234567890")
            .build();

    orderItems = new ArrayList<>();
    OrderItemDTO item = new OrderItemDTO();
    item.setProductId(1L);
    item.setQuantity(2);
    orderItems.add(item);

    orderCreateDTO = new OrderCreateDTO();
    orderCreateDTO.setEmail("test@example.com");
    orderCreateDTO.setShippingName("John Doe");
    orderCreateDTO.setShippingAddress("123 Main St");
    orderCreateDTO.setShippingPhone("+1234567890");
    orderCreateDTO.setItems(orderItems);
  }

  @Test
  void findOrderById_Success() {
    // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

    // When
    Order result = orderService.findOrderById(1L);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("test@example.com", result.getCustomerEmail());
  }

  @Test
  void findOrderById_InvalidId_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrderById(0L);
            });

    assertEquals("ID must be positive", exception.getMessage());
  }

  @Test
  void findOrderById_NegativeId_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrderById(-1L);
            });

    assertEquals("ID must be positive", exception.getMessage());
  }

  @Test
  void findOrderById_NotFound_ThrowsException() {
    // Given
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.findOrderById(999L);
            });

    assertEquals("id", exception.getMessage());
  }

  @Test
  void findAllOrders_Success() {
    // Given
    List<Order> orders = Arrays.asList(testOrder);
    Page<Order> page = new PageImpl<>(orders, pageRequest, orders.size());
    when(orderRepository.findAll(pageRequest)).thenReturn(page);

    // When
    Page<Order> result = orderService.findAllOrders(pageRequest);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("test@example.com", result.getContent().get(0).getCustomerEmail());
  }

  @Test
  void findAllOrders_EmptyList_Success() {
    // Given
    Page<Order> page = new PageImpl<>(new ArrayList<>(), pageRequest, 0);
    when(orderRepository.findAll(pageRequest)).thenReturn(page);

    // When
    Page<Order> result = orderService.findAllOrders(pageRequest);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  void findOrdersByStatus_Success() {
    // Given
    List<Order> orders = Arrays.asList(testOrder);
    Page<Order> page = new PageImpl<>(orders, pageRequest, orders.size());
    when(orderRepository.findByStatus(Status.PENDING, pageRequest)).thenReturn(page);

    // When
    Page<Order> result = orderService.findOrdersByStatus(Status.PENDING, pageRequest);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(Status.PENDING, result.getContent().get(0).getStatus());
  }

  @Test
  void findOrdersByStatus_EmptyList_Success() {
    // Given
    Page<Order> page = new PageImpl<>(new ArrayList<>(), pageRequest, 0);
    when(orderRepository.findByStatus(Status.CANCELLED, pageRequest)).thenReturn(page);

    // When
    Page<Order> result = orderService.findOrdersByStatus(Status.CANCELLED, pageRequest);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  void findOrdersByStatus_RepositoryException_ThrowsOrderException() {
    // Given
    when(orderRepository.findByStatus(any(), any()))
        .thenThrow(new RuntimeException("Database error"));

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.findOrdersByStatus(Status.PENDING, pageRequest);
            });

    assertTrue(exception.getMessage().contains("Error searching orders by status"));
  }

  @Test
  void findOrdersByMonth_Success() {
    // Given
    List<Order> orders = Arrays.asList(testOrder);
    Page<Order> page = new PageImpl<>(orders, Pageable.unpaged(), orders.size());

    LocalDateTime startOfMonth =
        LocalDateTime.now().withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

    when(orderRepository.findBetweenDates(
            any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
        .thenReturn(page);

    // When
    List<Order> result = orderService.findOrdersByMonth(1);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("test@example.com", result.get(0).getCustomerEmail());
  }

  @Test
  void findOrdersByMonth_InvalidMonth_TooSmall_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrdersByMonth(-1);
            });

    assertEquals("Month cannot be less than 0", exception.getMessage());
  }

  @Test
  void findOrdersByMonth_InvalidMonth_TooLarge_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrdersByMonth(13);
            });

    assertEquals("Month cannot be greater than 12", exception.getMessage());
  }

  @Test
  void findOrdersByMonth_RepositoryException_ThrowsOrderException() {
    // Given
    when(orderRepository.findBetweenDates(
            any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
        .thenThrow(new RuntimeException("Database error"));

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.findOrdersByMonth(1);
            });

    assertTrue(exception.getMessage().contains("Error retrieving orders by month"));
  }

  @Test
  void findOrdersBetweenDates_Success() {
    // Given
    List<Order> orders = Arrays.asList(testOrder);
    Page<Order> page = new PageImpl<>(orders, pageRequest, orders.size());

    LocalDateTime startDate = LocalDateTime.now().minusDays(7);
    LocalDateTime endDate = LocalDateTime.now();

    when(orderRepository.findBetweenDates(startDate, endDate, pageRequest)).thenReturn(page);

    // When
    Page<Order> result = orderService.findOrdersBetweenDates(startDate, endDate, pageRequest);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void findOrdersBetweenDates_NullStartDate_ThrowsException() {
    // Given
    LocalDateTime endDate = LocalDateTime.now();

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrdersBetweenDates(null, endDate, pageRequest);
            });

    assertEquals("Start and end dates cannot be null", exception.getMessage());
  }

  @Test
  void findOrdersBetweenDates_NullEndDate_ThrowsException() {
    // Given
    LocalDateTime startDate = LocalDateTime.now();

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrdersBetweenDates(startDate, null, pageRequest);
            });

    assertEquals("Start and end dates cannot be null", exception.getMessage());
  }

  @Test
  void findOrdersBetweenDates_StartDateAfterEndDate_ThrowsException() {
    // Given
    LocalDateTime startDate = LocalDateTime.now();
    LocalDateTime endDate = LocalDateTime.now().minusDays(1);

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.findOrdersBetweenDates(startDate, endDate, pageRequest);
            });

    assertEquals("Start date must be before end date", exception.getMessage());
  }

  @Test
  void findOrdersBetweenDates_RepositoryException_ThrowsOrderException() {
    // Given
    LocalDateTime startDate = LocalDateTime.now().minusDays(7);
    LocalDateTime endDate = LocalDateTime.now();

    when(orderRepository.findBetweenDates(startDate, endDate, pageRequest))
        .thenThrow(new RuntimeException("Database error"));

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.findOrdersBetweenDates(startDate, endDate, pageRequest);
            });

    assertTrue(exception.getMessage().contains("Error searching orders between dates"));
  }

  @Test
  void updateOrderStatus_Success() {
    // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // When
    Order result = orderService.updateOrderStatus(1L, Status.CONFIRMED);

    // Then
    assertNotNull(result);
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void updateOrderStatus_InvalidId_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.updateOrderStatus(0L, Status.CONFIRMED);
            });

    assertEquals("ID must be positive", exception.getMessage());
  }

  @Test
  void updateOrderStatus_NullStatus_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.updateOrderStatus(1L, null);
            });

    assertEquals("New status cannot be null", exception.getMessage());
  }

  @Test
  void updateOrderStatus_OrderNotFound_ThrowsException() {
    // Given
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.updateOrderStatus(999L, Status.CONFIRMED);
            });

    assertTrue(exception.getMessage().contains("Order not found for status update"));
  }

  @Test
  void createOrder_Success() {
    // Given
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // When
    Order result = orderService.createOrder(orderCreateDTO);

    // Then
    assertNotNull(result);
    assertEquals("test@example.com", result.getCustomerEmail());
    assertEquals("John Doe", result.getShippingName());
    assertEquals("123 Main St", result.getShippingAddress());
    assertEquals("+1234567890", result.getShippingPhone());
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void createOrder_WithUser_Success() {
    // Given
    orderCreateDTO.setUserId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // When
    Order result = orderService.createOrder(orderCreateDTO);

    // Then
    assertNotNull(result);
    verify(userRepository).findById(1L);
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void createOrder_NullDTO_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(null);
            });

    assertEquals("Order data cannot be null", exception.getMessage());
  }

  @Test
  void createOrder_InvalidEmail_ThrowsException() {
    // Given
    orderCreateDTO.setEmail("invalid-email");

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Invalid email", exception.getMessage());
  }

  @Test
  void createOrder_NullEmail_ThrowsException() {
    // Given
    orderCreateDTO.setEmail(null);

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Invalid email", exception.getMessage());
  }

  @Test
  void createOrder_NullShippingName_ThrowsException() {
    // Given
    orderCreateDTO.setShippingName(null);

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Shipping name is required", exception.getMessage());
  }

  @Test
  void createOrder_EmptyShippingName_ThrowsException() {
    // Given
    orderCreateDTO.setShippingName("  ");

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Shipping name is required", exception.getMessage());
  }

  @Test
  void createOrder_NullShippingAddress_ThrowsException() {
    // Given
    orderCreateDTO.setShippingAddress(null);

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Shipping address is required", exception.getMessage());
  }

  @Test
  void createOrder_EmptyShippingAddress_ThrowsException() {
    // Given
    orderCreateDTO.setShippingAddress("  ");

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Shipping address is required", exception.getMessage());
  }

  @Test
  void createOrder_NullShippingPhone_ThrowsException() {
    // Given
    orderCreateDTO.setShippingPhone(null);

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Invalid phone number", exception.getMessage());
  }

  @Test
  void createOrder_InvalidShippingPhone_ThrowsException() {
    // Given
    orderCreateDTO.setShippingPhone("123");

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("Invalid phone number", exception.getMessage());
  }

  @Test
  void createOrder_UserNotFound_ThrowsException() {
    // Given
    orderCreateDTO.setUserId(999L);
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () -> {
              orderService.createOrder(orderCreateDTO);
            });

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void deleteOrder_Success() {
    // Given
    when(orderRepository.existsById(1L)).thenReturn(true);
    doNothing().when(orderRepository).deleteById(1L);

    // When
    orderService.deleteOrder(1L);

    // Then
    verify(orderRepository).deleteById(1L);
  }

  @Test
  void deleteOrder_InvalidId_ThrowsException() {
    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.deleteOrder(0L);
            });

    assertEquals("ID must be positive", exception.getMessage());
  }

  @Test
  void deleteOrder_NotFound_ThrowsException() {
    // Given
    when(orderRepository.existsById(999L)).thenReturn(false);

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.deleteOrder(999L);
            });

    assertEquals("Order not found with ID: 999", exception.getMessage());
  }

  @Test
  void deleteOrder_RepositoryException_ThrowsOrderException() {
    // Given
    when(orderRepository.existsById(1L)).thenReturn(true);
    doThrow(new RuntimeException("Database error")).when(orderRepository).deleteById(1L);

    // When/Then
    OrderException exception =
        assertThrows(
            OrderException.class,
            () -> {
              orderService.deleteOrder(1L);
            });

    assertTrue(exception.getMessage().contains("Error deleting order"));
  }
}
