package com.shopflow.product.service;

import com.shopflow.product.dto.ProductRequest;
import com.shopflow.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    List<ProductResponse> findAll();
}