package com.shopflow.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String code,
        String path,
        List<FieldErrorDetail> errors
) {
}