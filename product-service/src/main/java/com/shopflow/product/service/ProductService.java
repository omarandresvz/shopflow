package com.shopflow.product.service;

import com.shopflow.product.dto.ProductRequest;
import com.shopflow.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    void decreaseStock(Long productId, Integer quantity);
}