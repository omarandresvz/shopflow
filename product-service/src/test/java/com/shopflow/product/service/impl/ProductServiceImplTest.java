package com.shopflow.product.service.impl;

import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.response.ProductResponse;
import com.shopflow.product.entity.Product;
import com.shopflow.product.exception.custom.InsufficientStockException;
import com.shopflow.product.exception.custom.ProductNotFoundException;
import com.shopflow.product.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Notebook")
                .description("Notebook gamer")
                .price(1200.0)
                .stock(10)
                .active(true)
                .build();

        request = new ProductRequest(
                "Notebook",
                "Notebook gamer",
                1200.0,
                10
        );
    }

    @Test
    void shouldCreateProductSuccessfully() {
        when(repository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Notebook");
        assertThat(response.description()).isEqualTo("Notebook gamer");
        assertThat(response.price()).isEqualTo(1200.0);
        assertThat(response.stock()).isEqualTo(10);
        assertThat(response.active()).isTrue();

        verify(repository).save(any(Product.class));
    }

    @Test
    void shouldFindAllActiveProducts() {
        when(repository.findByActiveTrue()).thenReturn(List.of(product));

        List<ProductResponse> response = service.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(0).name()).isEqualTo("Notebook");

        verify(repository).findByActiveTrue();
    }

    @Test
    void shouldFindProductByIdSuccessfully() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        ProductResponse response = service.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Notebook");
        assertThat(response.stock()).isEqualTo(10);

        verify(repository).findByIdAndActiveTrue(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundById() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository).findByIdAndActiveTrue(1L);
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        ProductRequest updateRequest = new ProductRequest(
                "Mouse",
                "Mouse inalámbrico",
                25.0,
                50
        );

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Mouse")
                .description("Mouse inalámbrico")
                .price(25.0)
                .stock(50)
                .active(true)
                .build();

        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(updatedProduct);

        ProductResponse response = service.update(1L, updateRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Mouse");
        assertThat(response.description()).isEqualTo("Mouse inalámbrico");
        assertThat(response.price()).isEqualTo(25.0);
        assertThat(response.stock()).isEqualTo(50);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingProduct() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(product);

        service.delete(1L);

        assertThat(product.getActive()).isFalse();

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingProduct() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void shouldDecreaseStockSuccessfully() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(product);

        service.decreaseStock(1L, 3);

        assertThat(product.getStock()).isEqualTo(7);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.decreaseStock(1L, 20))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(product.getStock()).isEqualTo(10);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void shouldIncreaseStockSuccessfully() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(product);

        service.increaseStock(1L, 5);

        assertThat(product.getStock()).isEqualTo(15);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenIncreasingStockForNonExistingProduct() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.increaseStock(1L, 5))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository).findByIdAndActiveTrue(1L);
        verify(repository, never()).save(any(Product.class));
    }
}