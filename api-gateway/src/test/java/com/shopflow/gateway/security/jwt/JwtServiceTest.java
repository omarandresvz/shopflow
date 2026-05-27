package com.shopflow.gateway.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
    }

    @Test
    void shouldExtractClaimsFromValidToken() {
        String token = generateToken(1L, "admin@test.com", "ADMIN");

        var claims = jwtService.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("admin@test.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("userId", Integer.class)).isEqualTo(1);
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String token = generateToken(1L, "customer@test.com", "CUSTOMER");

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        boolean valid = jwtService.isTokenValid("invalid-token");

        assertThat(valid).isFalse();
    }

    @Test
    void shouldExtractSubject() {
        String token = generateToken(2L, "user@test.com", "CUSTOMER");

        String subject = jwtService.extractSubject(token);

        assertThat(subject).isEqualTo("user@test.com");
    }

    @Test
    void shouldExtractRole() {
        String token = generateToken(3L, "admin@test.com", "ADMIN");

        String role = jwtService.extractRole(token);

        assertThat(role).isEqualTo("ADMIN");
    }

    private String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}