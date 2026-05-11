package com.shopflow.shared.exception;

public enum CommonErrorCode implements ErrorCode {

    VALIDATION_ERROR("Error de validación"),
    INVALID_REQUEST("Solicitud inválida"),
    RESOURCE_NOT_FOUND("Recurso no encontrado"),
    UNAUTHORIZED("No autenticado o token inválido"),
    ACCESS_DENIED("No tienes permisos para este recurso"),
    INTERNAL_ERROR("Error interno del servidor"),
    BAD_CREDENTIALS("Credenciales inválidas");

    private final String message;

    CommonErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}