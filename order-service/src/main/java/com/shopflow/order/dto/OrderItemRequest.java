package com.shopflow.order.dto;

public record OrderItemRequest(
        Long productId,
        Integer quantity
) {
}