package com.shopflow.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.request.UpdateStockRequest;
import com.shopflow.product.dto.response.ProductResponse;
import com.shopflow.product.service.ProductService;
import com.shopflow.shared.exception.ErrorResponseFactory;
import com.shopflow.shared.security.filter.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private AccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private ErrorResponseFactory errorResponseFactory;

    @Test
    void shouldFindAllProducts() throws Exception {
        ProductResponse product = new ProductResponse(
                1L,
                "Notebook",
                "Notebook gamer",
                1200.0,
                10,
                true,
                null,
                null
        );

        when(service.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Notebook"))
                .andExpect(jsonPath("$[0].description").value("Notebook gamer"))
                .andExpect(jsonPath("$[0].price").value(1200.0))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).findAll();
    }

    @Test
    void shouldFindProductById() throws Exception {
        ProductResponse product = new ProductResponse(
                1L,
                "Notebook",
                "Notebook gamer",
                1200.0,
                10,
                true,
                null,
                null
        );

        when(service.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.description").value("Notebook gamer"))
                .andExpect(jsonPath("$.price").value(1200.0))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).findById(1L);
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest(
                "Notebook",
                "Notebook gamer",
                1200.0,
                10
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Notebook",
                "Notebook gamer",
                1200.0,
                10,
                true,
                null,
                null
        );

        when(service.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.description").value("Notebook gamer"))
                .andExpect(jsonPath("$.price").value(1200.0))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).create(any(ProductRequest.class));
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest(
                "Mouse",
                "Mouse inalámbrico",
                25.0,
                50
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Mouse",
                "Mouse inalámbrico",
                25.0,
                50,
                true,
                null,
                null
        );

        when(service.update(eq(1L), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Mouse"))
                .andExpect(jsonPath("$.description").value("Mouse inalámbrico"))
                .andExpect(jsonPath("$.price").value(25.0))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).update(eq(1L), any(ProductRequest.class));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void shouldDecreaseStockSuccessfully() throws Exception {
        UpdateStockRequest request = new UpdateStockRequest(3);

        doNothing().when(service).decreaseStock(1L, 3);

        mockMvc.perform(patch("/api/v1/products/1/stock/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(service).decreaseStock(1L, 3);
    }

    @Test
    void shouldIncreaseStockSuccessfully() throws Exception {
        UpdateStockRequest request = new UpdateStockRequest(5);

        doNothing().when(service).increaseStock(1L, 5);

        mockMvc.perform(patch("/api/v1/products/1/stock/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(service).increaseStock(1L, 5);
    }
}