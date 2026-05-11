package com.shopflow.shared.exception;

public record FieldErrorDetail(
        String field,
        String message
) {
}