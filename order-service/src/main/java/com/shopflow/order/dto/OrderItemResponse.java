package com.shopflow.order.dto;

public record OrderItemResponse(
        Long productId,
        String productName,
        Double unitPrice,
        Integer quantity,
        Double subtotal
) {
}