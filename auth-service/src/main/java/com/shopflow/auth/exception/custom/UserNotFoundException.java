package com.shopflow.auth.exception.custom;

import org.springframework.http.HttpStatus;

import com.shopflow.auth.exception.AuthErrorCode;
import com.shopflow.shared.exception.BusinessException;

public class UserNotFoundException extends BusinessException{

     public UserNotFoundException() {
        super(AuthErrorCode.USER_NOT_FOUND, HttpStatus.CONFLICT);
    }
    
}
