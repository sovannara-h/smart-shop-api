package com.ecommerce.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser
    void createOrder_Success() throws Exception {
        Product product = Product.builder()
            .name("Test Product")
            .price(new BigDecimal("99.99"))
            .stockInQuantity(10)
            .active(true)
            .attributes(Collections.emptyMap())
            .interactions(Collections.emptyMap())
            .numberOfReviews(0)
            .build();
        
        product = productRepository.save(product);

        OrderItem orderItem = OrderItem.builder()
            .product(product)
            .quantity(2)
            .unitPrice(product.getPrice())
            .build();

        Order order = Order.builder()
            .orderDate(LocalDateTime.now())
            .status(Order.Status.PENDING)
            .totalAmount(product.getPrice().multiply(new BigDecimal(2)))
            .items(Collections.singletonList(orderItem))
            .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void getOrdersByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/orders/status/PENDING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}