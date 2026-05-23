package com.shopflow.product.repository;

import com.shopflow.product.entity.Product;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class ProductRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ProductRepository repository;

    @Test
    void shouldFindOnlyActiveProducts() {
        Product activeProduct = Product.builder()
                .name("Notebook")
                .description("Notebook gamer")
                .price(1200.0)
                .stock(10)
                .active(true)
                .build();

        Product inactiveProduct = Product.builder()
                .name("Mouse")
                .description("Mouse inalámbrico")
                .price(25.0)
                .stock(50)
                .active(false)
                .build();

        repository.save(activeProduct);
        repository.save(inactiveProduct);

        List<Product> products = repository.findByActiveTrue();

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Notebook");
        assertThat(products.get(0).getActive()).isTrue();
    }

    @Test
    void shouldFindProductByIdWhenActiveIsTrue() {
        Product product = Product.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(80.0)
                .stock(20)
                .active(true)
                .build();

        Product saved = repository.save(product);

        var result = repository.findByIdAndActiveTrue(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getName()).isEqualTo("Keyboard");
        assertThat(result.get().getActive()).isTrue();
    }

    @Test
    void shouldNotFindProductByIdWhenActiveIsFalse() {
        Product product = Product.builder()
                .name("Monitor")
                .description("Gaming monitor")
                .price(300.0)
                .stock(5)
                .active(false)
                .build();

        Product saved = repository.save(product);

        var result = repository.findByIdAndActiveTrue(saved.getId());

        assertThat(result).isEmpty();
    }
} 