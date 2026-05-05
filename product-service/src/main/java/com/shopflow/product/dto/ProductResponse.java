package com.shopflow.product.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price
) {
}