package com.shopflow.product.service;

import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    void decreaseStock(Long productId, Integer quantity);
}