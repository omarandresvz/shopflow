package com.shopflow.product.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.product.exception.ProductErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException() {
        super(ProductErrorCode.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}