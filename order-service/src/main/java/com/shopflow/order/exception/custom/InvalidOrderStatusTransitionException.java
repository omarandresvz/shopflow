package com.shopflow.order.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.order.exception.OrderErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class InvalidOrderStatusTransitionException extends BusinessException {

    public InvalidOrderStatusTransitionException() {
        super(OrderErrorCode.INVALID_ORDER_STATUS_TRANSITION, HttpStatus.CONFLICT);
    }
}