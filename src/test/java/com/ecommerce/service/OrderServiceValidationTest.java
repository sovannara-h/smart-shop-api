package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.dto.OrderItemDTO;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.impl.OrderServiceImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests de validation pour OrderService.
 *
 * <p>Ces tests se concentrent sur la validation des entrées et vérifient que les contraintes de
 * validation sont respectées.
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceValidationTest {

  @Mock private OrderRepository orderRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private OrderServiceImpl orderService;

  private OrderCreateDTO validOrderDTO;

  @BeforeEach
  void setUp() {
    // Initialisation d'un DTO valide
    validOrderDTO = new OrderCreateDTO();
    validOrderDTO.setEmail("test@example.com");
    validOrderDTO.setShippingName("John Doe");
    validOrderDTO.setShippingAddress("123 Main St");
    validOrderDTO.setShippingPhone("+1234567890");

    List<OrderItemDTO> items = new ArrayList<>();
    OrderItemDTO item = new OrderItemDTO();
    item.setProductId(1L);
    item.setQuantity(2);
    items.add(item);

    validOrderDTO.setItems(items);
  }

  /**
   * Test paramétré pour les emails invalides. Cette approche permet de tester plusieurs valeurs
   * invalides dans un seul test.
   */
  @ParameterizedTest
  @ValueSource(
      strings = {"invalid-email", "missing-at.com", "@missing-start.com", "missing-domain@", ""})
  @DisplayName("Doit rejeter les emails invalides")
  void createOrder_InvalidEmail(String invalidEmail) {
    // Préparation
    OrderCreateDTO invalidOrderDTO = new OrderCreateDTO();
    invalidOrderDTO.setEmail(invalidEmail);
    invalidOrderDTO.setShippingName("John Doe");
    invalidOrderDTO.setShippingAddress("123 Main St");
    invalidOrderDTO.setShippingPhone("+1234567890");
    invalidOrderDTO.setItems(validOrderDTO.getItems());

    // Vérification
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(invalidOrderDTO);
            });

    assertEquals("Invalid email", exception.getMessage());
  }

  /** Test pour vérifier la validation du numéro de téléphone. */
  @Test
  @DisplayName("Doit rejeter les numéros de téléphone invalides")
  void createOrder_InvalidPhone() {
    // Préparation
    OrderCreateDTO invalidOrderDTO = new OrderCreateDTO();
    invalidOrderDTO.setEmail("test@example.com");
    invalidOrderDTO.setShippingName("John Doe");
    invalidOrderDTO.setShippingAddress("123 Main St");
    invalidOrderDTO.setShippingPhone("123"); // trop court
    invalidOrderDTO.setItems(validOrderDTO.getItems());

    // Vérification
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(invalidOrderDTO);
            });

    assertEquals("Invalid phone number", exception.getMessage());
  }

  /** Test pour vérifier la validation des champs obligatoires. */
  @Test
  @DisplayName("Doit rejeter les commandes sans nom de livraison")
  void createOrder_MissingShippingName() {
    // Préparation
    OrderCreateDTO invalidOrderDTO = new OrderCreateDTO();
    invalidOrderDTO.setEmail("test@example.com");
    invalidOrderDTO.setShippingName(null);
    invalidOrderDTO.setShippingAddress("123 Main St");
    invalidOrderDTO.setShippingPhone("+1234567890");
    invalidOrderDTO.setItems(validOrderDTO.getItems());

    // Vérification
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              orderService.createOrder(invalidOrderDTO);
            });

    assertEquals("Shipping name is required", exception.getMessage());
  }
}
