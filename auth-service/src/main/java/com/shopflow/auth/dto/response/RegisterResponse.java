package com.shopflow.auth.dto.response;

public record RegisterResponse(
        Long userId,
        String email,
        String message
) {
}