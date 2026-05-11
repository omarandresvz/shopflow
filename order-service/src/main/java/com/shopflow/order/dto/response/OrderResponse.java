package com.shopflow.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Double total,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}