package com.shopflow.security.model;

public record CurrentUser(
        Long userId,
        String email,
        String role
) {
}