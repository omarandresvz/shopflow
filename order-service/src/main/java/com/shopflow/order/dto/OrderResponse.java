package com.shopflow.order.dto;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Double total,
        String status,
        LocalDateTime createdAt
) {}