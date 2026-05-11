package com.shopflow.product.exception;

import com.shopflow.shared.exception.ErrorCode;

public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND("Producto no encontrado"),
    INSUFFICIENT_STOCK("Stock insuficiente"),
    PRODUCT_DISABLED("Producto no disponible");

    private final String message;

    ProductErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}