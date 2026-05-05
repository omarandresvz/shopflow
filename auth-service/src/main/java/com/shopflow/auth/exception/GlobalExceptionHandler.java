package com.shopflow.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 VALIDATION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        List<FieldErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorDetail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                ErrorCode.VALIDATION_ERROR,
                request.getRequestURI(),
                errors
        );
    }

    // 🔴 BUSINESS
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                ErrorCode.EMAIL_ALREADY_EXISTS,
                request.getRequestURI(),
                List.of()
        );
    }

    // 🔴 GENERIC
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error",
                ErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                List.of()
        );
    }

    // 🧩 builder central
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            ErrorCode code,
            String path,
            List<FieldErrorDetail> errors
    ) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                code.name(),
                path,
                errors
        );

        return ResponseEntity.status(status).body(response);
    }
}