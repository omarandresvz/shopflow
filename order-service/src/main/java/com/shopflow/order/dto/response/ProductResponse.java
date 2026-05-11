package com.shopflow.order.dto.response;

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