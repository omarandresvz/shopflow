package com.shopflow.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseFactoryTest {

    private final ErrorResponseFactory factory = new ErrorResponseFactory();

    @Test
    void shouldBuildErrorResponseWithoutFieldErrors() {
        ErrorResponse response = factory.build(
                HttpStatus.NOT_FOUND,
                CommonErrorCode.RESOURCE_NOT_FOUND,
                "/api/v1/products/99"
        );

        assertThat(response.timestamp()).isNotNull();
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("Recurso no encontrado");
        assertThat(response.code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.path()).isEqualTo("/api/v1/products/99");
        assertThat(response.errors()).isNull();
    }

    @Test
    void shouldBuildErrorResponseWithFieldErrors() {
        List<FieldErrorDetail> errors = List.of(
                new FieldErrorDetail("email", "El email es obligatorio"),
                new FieldErrorDetail("password", "La contraseña es obligatoria")
        );

        ErrorResponse response = factory.build(
                HttpStatus.BAD_REQUEST,
                CommonErrorCode.VALIDATION_ERROR,
                "/api/v1/auth/register",
                errors
        );

        assertThat(response.timestamp()).isNotNull();
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.message()).isEqualTo("Error de validación");
        assertThat(response.code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.path()).isEqualTo("/api/v1/auth/register");
        assertThat(response.errors()).hasSize(2);
        assertThat(response.errors().get(0).field()).isEqualTo("email");
        assertThat(response.errors().get(1).field()).isEqualTo("password");
    }
}