package com.shopflow.order.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.order.exception.OrderErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class OrderAccessDeniedException extends BusinessException {

    public OrderAccessDeniedException() {
        super(OrderErrorCode.ACCESS_DENIED_ORDER_OPERATION, HttpStatus.FORBIDDEN);
    }
}