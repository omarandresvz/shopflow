package com.shopflow.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.request.UpdateStockRequest;
import com.shopflow.product.entity.Product;
import com.shopflow.product.repository.ProductRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProductControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository repository;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void shouldFindAllActiveProducts() throws Exception {
        repository.save(Product.builder()
                .name("Notebook")
                .description("Notebook gamer")
                .price(1200.0)
                .stock(10)
                .active(true)
                .build());

        repository.save(Product.builder()
                .name("Mouse")
                .description("Mouse inalámbrico")
                .price(25.0)
                .stock(50)
                .active(false)
                .build());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Notebook"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldFindProductById() throws Exception {
        Product saved = repository.save(Product.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(80.0)
                .stock(20)
                .active(true)
                .build());

        mockMvc.perform(get("/api/v1/products/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void shouldDecreaseStock() throws Exception {
        Product saved = repository.save(Product.builder()
                .name("Monitor")
                .description("Gaming monitor")
                .price(300.0)
                .stock(10)
                .active(true)
                .build());

        UpdateStockRequest request = new UpdateStockRequest(3);

        mockMvc.perform(patch("/api/v1/products/{id}/stock/decrease", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Product updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStock()).isEqualTo(7);
    }

    @Test
    void shouldIncreaseStock() throws Exception {
        Product saved = repository.save(Product.builder()
                .name("Chair")
                .description("Gaming chair")
                .price(250.0)
                .stock(4)
                .active(true)
                .build());

        UpdateStockRequest request = new UpdateStockRequest(6);

        mockMvc.perform(patch("/api/v1/products/{id}/stock/increase", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Product updated = repository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStock()).isEqualTo(10);
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        Product saved = repository.save(Product.builder()
                .name("Headset")
                .description("Wireless headset")
                .price(100.0)
                .stock(2)
                .active(true)
                .build());

        UpdateStockRequest request = new UpdateStockRequest(5);

        mockMvc.perform(patch("/api/v1/products/{id}/stock/decrease", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProductWhenUserIsAdmin() throws Exception {
        ProductRequest request = new ProductRequest(
                "Tablet",
                "Android tablet",
                350.0,
                15
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Tablet"))
                .andExpect(jsonPath("$.price").value(350.0))
                .andExpect(jsonPath("$.stock").value(15))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(repository.findByActiveTrue()).hasSize(1);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenCustomerCreatesProduct() throws Exception {
        ProductRequest request = new ProductRequest(
                "Tablet",
                "Android tablet",
                350.0,
                15
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingProductWithoutAuthentication() throws Exception {
        ProductRequest request = new ProductRequest(
                "Tablet",
                "Android tablet",
                350.0,
                15
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}