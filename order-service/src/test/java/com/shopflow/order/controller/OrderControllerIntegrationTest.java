package com.shopflow.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.order.client.ProductClient;
import com.shopflow.order.dto.request.CreateOrderRequest;
import com.shopflow.order.dto.request.OrderItemRequest;
import com.shopflow.order.dto.response.ProductResponse;
import com.shopflow.order.entity.Order;
import com.shopflow.order.entity.OrderItem;
import com.shopflow.order.entity.OrderStatus;
import com.shopflow.order.repository.OrderRepository;
import com.shopflow.shared.security.model.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository repository;

    @MockitoBean
    private ProductClient productClient;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateOrderWhenCustomerIsAuthenticated() throws Exception {
        ProductResponse product = new ProductResponse(
                10L,
                "Keyboard",
                "Mechanical keyboard",
                50.0,
                20,
                true,
                LocalDateTime.now(),
                null
        );

        when(productClient.getProductById(10L))
                .thenReturn(product);

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(10L, 3))
        );

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(customerAuth(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.total").value(150.0))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(10L))
                .andExpect(jsonPath("$.items[0].productName").value("Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.items[0].subtotal").value(150.0));

        verify(productClient).getProductById(10L);
        verify(productClient).decreaseStock(10L, 3);

        assertThat(repository.findByUserId(1L)).hasSize(1);
    }

    @Test
    void shouldReturnMyOrdersWhenCustomerIsAuthenticated() throws Exception {
        Order order = Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(20L)
                .productName("Mouse")
                .unitPrice(25.0)
                .quantity(4)
                .subtotal(100.0)
                .build();

        order.addItem(item);

        repository.save(order);

        mockMvc.perform(get("/api/v1/orders/my")
                        .with(authentication(customerAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].total").value(100.0))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].items.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].productName").value("Mouse"));
    }

    @Test
    void shouldCancelOwnOrderAndRestoreStock() throws Exception {
        Order order = Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(30L)
                .productName("Monitor")
                .unitPrice(100.0)
                .quantity(1)
                .subtotal(100.0)
                .build();

        order.addItem(item);

        Order saved = repository.save(order);

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", saved.getId())
                        .with(authentication(customerAuth(1L))))
                .andExpect(status().isNoContent());

        Order updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(productClient).increaseStock(30L, 1);
    }

    @Test
    void shouldReturnForbiddenWhenCustomerCancelsAnotherUserOrder() throws Exception {
        Order order = repository.save(Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", order.getId())
                        .with(authentication(customerAuth(99L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPayOrderWhenAdminIsAuthenticated() throws Exception {
        Order order = repository.save(Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(patch("/api/v1/orders/{id}/pay", order.getId())
                        .with(authentication(adminAuth(99L))))
                .andExpect(status().isNoContent());

        Order updated = repository.findById(order.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldReturnForbiddenWhenCustomerPaysOrder() throws Exception {
        Order order = repository.save(Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(patch("/api/v1/orders/{id}/pay", order.getId())
                        .with(authentication(customerAuth(1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenRequestHasNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isUnauthorized());
    }

    private UsernamePasswordAuthenticationToken customerAuth(Long userId) {
        CurrentUser currentUser = new CurrentUser(
                userId,
                "customer@test.com",
                "CUSTOMER"
        );

        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    private UsernamePasswordAuthenticationToken adminAuth(Long userId) {
        CurrentUser currentUser = new CurrentUser(
                userId,
                "admin@test.com",
                "ADMIN"
        );

        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}