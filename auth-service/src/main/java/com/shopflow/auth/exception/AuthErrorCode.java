package com.shopflow.auth.exception;

import com.shopflow.shared.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {

    EMAIL_ALREADY_EXISTS("El e-mail ya se encuentra registrado"),
    USER_NOT_FOUND("Usuario no encontrado");

    private final String message;

    AuthErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}