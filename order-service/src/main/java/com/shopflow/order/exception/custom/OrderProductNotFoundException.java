package com.shopflow.order.exception.custom;

import com.shopflow.order.exception.OrderErrorCode;
import com.shopflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderProductNotFoundException extends BusinessException {

    public OrderProductNotFoundException() {
        super(OrderErrorCode.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}