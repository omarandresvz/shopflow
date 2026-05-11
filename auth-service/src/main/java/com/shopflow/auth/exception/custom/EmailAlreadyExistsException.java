package com.shopflow.auth.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.auth.exception.AuthErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super(AuthErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
    
}