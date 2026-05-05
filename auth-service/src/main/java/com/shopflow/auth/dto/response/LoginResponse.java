package com.shopflow.auth.dto.response;

public record LoginResponse(
        Long userId,
        String email,
        String role,
        String token
) {
}