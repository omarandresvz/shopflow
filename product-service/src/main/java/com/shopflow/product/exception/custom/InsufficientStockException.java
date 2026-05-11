package com.shopflow.product.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.product.exception.ProductErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException() {
        super(ProductErrorCode.INSUFFICIENT_STOCK, HttpStatus.CONFLICT);
    }
}