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
import com.shopflow.order.exception.custom.OrderNotFoundException;
import com.shopflow.order.repository.OrderRepository;
import com.shopflow.shared.security.model.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderServiceImpl service;

    private ProductResponse product;
    private CreateOrderRequest request;
    private Order order;

    @BeforeEach
    void setUp() {
        product = new ProductResponse(
                1L,
                "Notebook",
                "Notebook gamer",
                1000.0,
                10,
                true,
                null,
                null
        );

        request = new CreateOrderRequest(
                List.of(new OrderItemRequest(1L, 2))
        );

        order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .total(2000.0)
                .build();

        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productName("Notebook")
                .unitPrice(1000.0)
                .quantity(2)
                .subtotal(2000.0)
                .build();

        order.addItem(item);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        when(productClient.getProductById(1L)).thenReturn(product);
        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        OrderResponse response = service.create(10L, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.total()).isEqualTo(2000.0);
        assertThat(response.status()).isEqualTo("CREATED");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productId()).isEqualTo(1L);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.items().get(0).subtotal()).isEqualTo(2000.0);

        verify(productClient).getProductById(1L);
        verify(productClient).decreaseStock(1L, 2);
        verify(repository).save(any(Order.class));
    }

    @Test
    void shouldPropagateExceptionWhenProductDoesNotExist() {
        when(productClient.getProductById(1L))
                .thenThrow(new RuntimeException("Producto no encontrado"));

        assertThatThrownBy(() -> service.create(10L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Producto no encontrado");

        verify(productClient).getProductById(1L);
        verify(productClient, never()).decreaseStock(anyLong(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldPayOrderSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        service.payOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        verify(repository).findById(1L);
        verify(repository).save(order);
    }

    @Test
    void shouldThrowExceptionWhenPayTransitionIsInvalid() {
        order.setStatus(OrderStatus.PAID);
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.payOrder(1L))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(repository).findById(1L);
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldShipOrderSuccessfully() {
        order.setStatus(OrderStatus.PAID);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        service.shipOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        verify(repository).findById(1L);
        verify(repository).save(order);
    }

    @Test
    void shouldDeliverOrderSuccessfully() {
        order.setStatus(OrderStatus.SHIPPED);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        service.deliverOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        verify(repository).findById(1L);
        verify(repository).save(order);
    }

    @Test
    void shouldCancelOwnOrderSuccessfullyAndRestoreStock() {
        setCustomerAuthentication(10L);

        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        service.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(repository).findById(1L);
        verify(productClient).increaseStock(1L, 2);
        verify(repository).save(order);
    }

    @Test
    void shouldCancelAnyOrderWhenUserIsAdmin() {
        setAdminAuthentication(99L);

        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        service.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(productClient).increaseStock(1L, 2);
        verify(repository).save(order);
    }

    @Test
    void shouldThrowExceptionWhenCustomerCancelsAnotherUserOrder() {
        setCustomerAuthentication(99L);

        when(repository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(1L))
                .isInstanceOf(OrderAccessDeniedException.class);

        verify(repository).findById(1L);
        verify(productClient, never()).increaseStock(anyLong(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenCancelTransitionIsInvalid() {
        setCustomerAuthentication(10L);

        order.setStatus(OrderStatus.SHIPPED);
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(1L))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(repository).findById(1L);
        verify(productClient, never()).increaseStock(anyLong(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.payOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(repository).findById(1L);
        verify(repository, never()).save(any(Order.class));
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

    @Test
    void shouldGetMyOrdersSuccessfully() {
        when(repository.findByUserId(10L)).thenReturn(List.of(order));

        List<OrderResponse> response = service.getMyOrders(10L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(0).total()).isEqualTo(2000.0);
        assertThat(response.get(0).status()).isEqualTo("CREATED");

        verify(repository).findByUserId(10L);
    }

    @Test
    void shouldThrowExceptionWhenShipTransitionIsInvalid() {
        order.setStatus(OrderStatus.CREATED);
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.shipOrder(1L))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(repository).findById(1L);
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenDeliverTransitionIsInvalid() {
        order.setStatus(OrderStatus.PAID);
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.deliverOrder(1L))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(repository).findById(1L);
        verify(repository, never()).save(any(Order.class));
    }
}