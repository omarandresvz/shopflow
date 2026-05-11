package com.shopflow.order.dto.response;

public record OrderItemResponse(
        Long productId,
        String productName,
        Double unitPrice,
        Integer quantity,
        Double subtotal
) {
}