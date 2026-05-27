package com.shopflow.shared.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void shouldGenerateTokenWithExpectedClaims() {
        String token = jwtService.generateToken(1L, "user@test.com", "ADMIN");

        Claims claims = jwtService.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user@test.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void shouldExtractSubject() {
        String token = jwtService.generateToken(10L, "customer@test.com", "CUSTOMER");

        String subject = jwtService.extractSubject(token);

        assertThat(subject).isEqualTo("customer@test.com");
    }

    @Test
    void shouldExtractRole() {
        String token = jwtService.generateToken(10L, "customer@test.com", "CUSTOMER");

        String role = jwtService.extractRole(token);

        assertThat(role).isEqualTo("CUSTOMER");
    }

    @Test
    void shouldExtractUserId() {
        String token = jwtService.generateToken(99L, "admin@test.com", "ADMIN");

        Long userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo(99L);
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String token = jwtService.generateToken(1L, "user@test.com", "CUSTOMER");

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        boolean valid = jwtService.isTokenValid("invalid-token");

        assertThat(valid).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        String token = jwtService.generateToken(1L, "user@test.com", "CUSTOMER");

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isFalse();
    }
}