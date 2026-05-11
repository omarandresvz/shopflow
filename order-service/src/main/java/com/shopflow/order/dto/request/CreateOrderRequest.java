package com.shopflow.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "La orden debe contener al menos un producto")
        List<@Valid OrderItemRequest> items
) {
}