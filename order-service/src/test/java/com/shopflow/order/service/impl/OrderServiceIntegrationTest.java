package com.shopflow.order.service.impl;

import com.shopflow.order.client.ProductClient;
import com.shopflow.order.dto.request.CreateOrderRequest;
import com.shopflow.order.dto.request.OrderItemRequest;
import com.shopflow.order.dto.response.OrderResponse;
import com.shopflow.order.dto.response.ProductResponse;
import com.shopflow.order.entity.Order;
import com.shopflow.order.entity.OrderItem;
import com.shopflow.order.entity.OrderStatus;
import com.shopflow.order.exception.custom.InvalidOrderStatusTransitionException;
import com.shopflow.order.exception.custom.OrderAccessDeniedException;
import com.shopflow.order.repository.OrderRepository;
import com.shopflow.shared.security.model.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private OrderServiceImpl service;

    @Autowired
    private OrderRepository repository;

    @MockitoBean
    private ProductClient productClient;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        repository.deleteAll();
    }

    @Test
    void shouldCreateOrderAndDecreaseStock() {
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

        OrderResponse response = service.create(1L, request);

        assertThat(response.id()).isNotNull();
        assertThat(response.total()).isEqualTo(150.0);
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(response.items()).hasSize(1);

        assertThat(response.items().get(0).productId()).isEqualTo(10L);
        assertThat(response.items().get(0).productName()).isEqualTo("Keyboard");
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.items().get(0).subtotal()).isEqualTo(150.0);

        verify(productClient).getProductById(10L);
        verify(productClient).decreaseStock(10L, 3);

        List<Order> orders = repository.findByUserId(1L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getTotal()).isEqualTo(150.0);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldReturnMyOrders() {
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

        List<OrderResponse> response = service.getMyOrders(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).total()).isEqualTo(100.0);
        assertThat(response.get(0).items()).hasSize(1);
        assertThat(response.get(0).items().get(0).productName()).isEqualTo("Mouse");
    }

    @Test
    void shouldPayOrderWhenStatusIsCreated() {
        Order order = repository.save(Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        service.payOrder(order.getId());

        Order updated = repository.findById(order.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldThrowWhenPayOrderWithInvalidStatus() {
        Order order = repository.save(Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.SHIPPED)
                .createdAt(LocalDateTime.now())
                .build());

        assertThatThrownBy(() -> service.payOrder(order.getId()))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void shouldCancelOwnOrderAndRestoreStock() {
        setCustomerAuthentication(1L);

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

        service.cancelOrder(saved.getId());

        Order updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(productClient).increaseStock(30L, 1);
    }

    @Test
    void shouldThrowWhenCustomerCancelsAnotherUserOrder() {
        setCustomerAuthentication(99L);

        Order order = repository.save(Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        assertThatThrownBy(() -> service.cancelOrder(order.getId()))
                .isInstanceOf(OrderAccessDeniedException.class);
    }

    @Test
    void shouldAllowAdminToCancelAnyOrder() {
        setAdminAuthentication(99L);

        Order order = Order.builder()
                .userId(1L)
                .total(50.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(40L)
                .productName("Chair")
                .unitPrice(50.0)
                .quantity(1)
                .subtotal(50.0)
                .build();

        order.addItem(item);

        Order saved = repository.save(order);

        service.cancelOrder(saved.getId());

        Order updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(productClient).increaseStock(40L, 1);
    }

    private void setCustomerAuthentication(Long userId) {
        CurrentUser currentUser = new CurrentUser(
                userId,
                "customer@test.com",
                "CUSTOMER"
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAdminAuthentication(Long userId) {
        CurrentUser currentUser = new CurrentUser(
                userId,
                "admin@test.com",
                "ADMIN"
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}