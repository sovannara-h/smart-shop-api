package com.ecommerce.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.dto.OrderItemDTO;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.ProductVariant;
import com.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProductRepository productRepository;

  @Test
  @WithMockUser
  void createOrder_Success() throws Exception {
    Product product =
        Product.builder()
            .name("Test Product")
            .hasVariants(true)
            .variants(
                new HashSet<>(
                    Arrays.asList(
                        ProductVariant.builder()
                            .sku("TEST-1")
                            .price(new BigDecimal("99.99"))
                            .stockQuantity(10)
                            .build())))
            .active(true)
            .build();

    product = productRepository.save(product);

    OrderCreateDTO orderDTO = new OrderCreateDTO();
    orderDTO.setEmail("test@example.com");
    orderDTO.setShippingName("John Doe");
    orderDTO.setShippingAddress("123 Test St");
    orderDTO.setShippingPhone("0123456789");
    orderDTO.setItems(List.of(new OrderItemDTO(product.getId(), 2)));

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @WithMockUser
  void getOrdersByStatus_Success() throws Exception {
    mockMvc
        .perform(get("/api/orders/status/PENDING").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray());
  }
}
