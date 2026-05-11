package com.shopflow.order.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.order.exception.OrderErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException() {
        super(OrderErrorCode.INSUFFICIENT_STOCK, HttpStatus.CONFLICT);
    }
}