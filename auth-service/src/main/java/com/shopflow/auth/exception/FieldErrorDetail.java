package com.shopflow.auth.exception;

public record FieldErrorDetail(
        String field,
        String message
) {
}