package com.shopflow.product.dto;

public record ProductRequest(
        String name,
        String description,
        Double price
) {
}