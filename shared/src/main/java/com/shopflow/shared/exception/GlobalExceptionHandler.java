package com.shopflow.shared.exception;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
            ex.getStatus(),
            ex.getErrorCode(),
            request.getRequestURI()
        );

        return ResponseEntity
            .status(ex.getStatus())
            .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .toList();

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                CommonErrorCode.VALIDATION_ERROR,
                request.getRequestURI(),
                errors
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.FORBIDDEN,
                CommonErrorCode.ACCESS_DENIED,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.NOT_FOUND,
                CommonErrorCode.RESOURCE_NOT_FOUND,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                CommonErrorCode.INTERNAL_ERROR,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private FieldErrorDetail mapFieldError(FieldError fieldError) {
        return new FieldErrorDetail(
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );
    }

    // contraseña incorrecta
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
        BadCredentialsException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
            HttpStatus.UNAUTHORIZED,
            CommonErrorCode.BAD_CREDENTIALS,
            request.getRequestURI()
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
    }

    // email inexistente
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
        InternalAuthenticationServiceException ex,
        HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
            HttpStatus.UNAUTHORIZED,
            CommonErrorCode.BAD_CREDENTIALS,
            request.getRequestURI()
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
    }
}