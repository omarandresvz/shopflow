package com.shopflow.order.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.order.exception.OrderErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class OrderNotFoundException extends BusinessException{

        public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    
}
