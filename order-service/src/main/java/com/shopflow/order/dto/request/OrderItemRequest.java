package com.shopflow.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(

        @NotNull(message = "El ID del producto es obligatorio")
        @Positive(message = "El ID del producto debe ser mayor que cero")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer quantity
) {
        
}