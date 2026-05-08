package com.shopflow.order.dto;

import java.util.List;

public record CreateOrderRequest(
        List<OrderItemRequest> items
) {
}