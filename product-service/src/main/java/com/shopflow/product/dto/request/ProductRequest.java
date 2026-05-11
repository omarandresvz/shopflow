package com.shopflow.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150, message = "El nombre del producto no puede superar los 150 caracteres")
        String name,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String description,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor que cero")
        Double price,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock debe ser cero o mayor")
        Integer stock
) {
}