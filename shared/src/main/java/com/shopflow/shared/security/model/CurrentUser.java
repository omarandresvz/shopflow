package com.shopflow.shared.security.model;

public record CurrentUser(
        Long userId,
        String email,
        String role
) {
}