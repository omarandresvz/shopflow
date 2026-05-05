package com.shopflow.auth.exception;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura estándar de error")
public record ErrorResponse(
    @Schema(example = "2026-04-08T12:00:00")
    LocalDateTime timestamp,

    @Schema(example = "400")
    int status,

    @Schema(example = "Bad Request")
    String error,

    @Schema(example = "Error de validación")
    String message,

    @Schema(example = "VALIDATION_ERROR")
    String code,

    @Schema(example = "/api/auth/login")
    String path,

    List<FieldErrorDetail> errors 
) {}