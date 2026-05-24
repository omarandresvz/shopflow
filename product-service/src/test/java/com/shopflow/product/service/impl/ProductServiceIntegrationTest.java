package com.shopflow.product.service.impl;

import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.response.ProductResponse;
import com.shopflow.product.entity.Product;
import com.shopflow.product.exception.custom.InsufficientStockException;
import com.shopflow.product.exception.custom.ProductNotFoundException;
import com.shopflow.product.repository.ProductRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ProductServiceImpl service;

    @Autowired
    private ProductRepository repository;

    @Test
    void shouldCreateProduct() {
        ProductRequest request = new ProductRequest(
                "Notebook",
                "Notebook gamer",
                1200.0,
                10
        );

        ProductResponse response = service.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Notebook");
        assertThat(response.price()).isEqualTo(1200.0);
        assertThat(response.stock()).isEqualTo(10);
        assertThat(response.active()).isTrue();

        assertThat(repository.findById(response.id())).isPresent();
    }

    @Test
    void shouldFindProductByIdWhenActive() {
        Product saved = repository.save(Product.builder()
                .name("Mouse")
                .description("Mouse inalámbrico")
                .price(25.0)
                .stock(50)
                .active(true)
                .build());

        ProductResponse response = service.findById(saved.getId());

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Mouse");
        assertThat(response.active()).isTrue();
    }

    @Test
    void shouldThrowProductNotFoundWhenProductDoesNotExist() {
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldSoftDeleteProduct() {
        Product saved = repository.save(Product.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(80.0)
                .stock(20)
                .active(true)
                .build());

        service.delete(saved.getId());

        Product deleted = repository.findById(saved.getId()).orElseThrow();

        assertThat(deleted.getActive()).isFalse();
        assertThat(service.findAll()).doesNotContain(
                new ProductResponse(
                        deleted.getId(),
                        deleted.getName(),
                        deleted.getDescription(),
                        deleted.getPrice(),
                        deleted.getStock(),
                        deleted.getActive(),
                        deleted.getCreatedAt(),
                        deleted.getUpdatedAt()
                )
        );
    }

    @Test
    void shouldDecreaseStock() {
        Product saved = repository.save(Product.builder()
                .name("Monitor")
                .description("Gaming monitor")
                .price(300.0)
                .stock(10)
                .active(true)
                .build());

        service.decreaseStock(saved.getId(), 3);

        Product updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStock()).isEqualTo(7);
    }

    @Test
    void shouldThrowInsufficientStockWhenQuantityIsGreaterThanStock() {
        Product saved = repository.save(Product.builder()
                .name("Headset")
                .description("Wireless headset")
                .price(100.0)
                .stock(2)
                .active(true)
                .build());

        assertThatThrownBy(() -> service.decreaseStock(saved.getId(), 5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void shouldIncreaseStock() {
        Product saved = repository.save(Product.builder()
                .name("Chair")
                .description("Gaming chair")
                .price(250.0)
                .stock(4)
                .active(true)
                .build());

        service.increaseStock(saved.getId(), 6);

        Product updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStock()).isEqualTo(10);
    }
}