package com.shopflow.auth.dto.response;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role
) {
}