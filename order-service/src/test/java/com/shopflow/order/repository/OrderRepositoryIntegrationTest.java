package com.shopflow.order.repository;

import com.shopflow.order.entity.Order;
import com.shopflow.order.entity.OrderItem;
import com.shopflow.order.entity.OrderStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private OrderRepository repository;

    @Test
    void shouldFindOrdersByUserId() {
        Order orderUserOne = Order.builder()
                .userId(1L)
                .total(100.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Order orderUserTwo = Order.builder()
                .userId(2L)
                .total(200.0)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(orderUserOne);
        repository.save(orderUserTwo);

        List<Order> result = repository.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getTotal()).isEqualTo(100.0);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoOrders() {
        List<Order> result = repository.findByUserId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveOrderWithItems() {
        Order order = Order.builder()
                .userId(1L)
                .total(150.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(10L)
                .productName("Keyboard")
                .unitPrice(50.0)
                .quantity(3)
                .subtotal(150.0)
                .build();

        order.addItem(item);

        Order saved = repository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getProductId()).isEqualTo(10L);
        assertThat(saved.getItems().get(0).getOrder()).isEqualTo(saved);
    }
}