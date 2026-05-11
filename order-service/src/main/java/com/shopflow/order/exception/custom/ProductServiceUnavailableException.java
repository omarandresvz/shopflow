package com.shopflow.order.exception.custom;

import com.shopflow.order.exception.OrderErrorCode;
import com.shopflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProductServiceUnavailableException extends BusinessException {

    public ProductServiceUnavailableException(Throwable cause) {
        super(
                OrderErrorCode.PRODUCT_SERVICE_UNAVAILABLE,
                HttpStatus.SERVICE_UNAVAILABLE,
                cause
        );
    }
}