package com.shopflow.order.dto;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        Integer stock,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}