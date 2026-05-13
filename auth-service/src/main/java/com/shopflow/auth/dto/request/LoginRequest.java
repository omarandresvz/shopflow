package com.shopflow.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Schema(example = "admin@shopflow.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 160, message = "El email no puede superar los 160 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}