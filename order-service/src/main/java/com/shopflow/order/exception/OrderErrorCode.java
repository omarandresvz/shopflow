package com.shopflow.order.exception;

import com.shopflow.shared.exception.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND("El producto solicitado no existe"),
    PRODUCT_SERVICE_UNAVAILABLE("No se pudo consultar el servicio de productos"),
    INSUFFICIENT_STOCK("Stock insuficiente para crear la orden"),
    INVALID_ORDER_STATUS_TRANSITION("La orden no puede cambiar a ese estado"),
    ORDER_NOT_FOUND("Orden no encontrada"),
    ACCESS_DENIED_ORDER_OPERATION("No tienes permisos para operar esta orden");

    private final String message;

    OrderErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}