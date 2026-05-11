package com.shopflow.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

//@Component
public class ErrorResponseFactory {

    public ErrorResponse build(
            HttpStatus status,
            ErrorCode errorCode,
            String path
    ) {
        return build(status, errorCode, path, null);
    }

    public ErrorResponse build(
            HttpStatus status,
            ErrorCode errorCode,
            String path,
            List<FieldErrorDetail> errors
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode.getMessage(),
                errorCode.name(),
                path,
                errors
        );
    }
}