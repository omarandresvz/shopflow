package com.shopflow.gateway.security.converter;

import com.shopflow.gateway.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationConverterTest {

    private final JwtService jwtService = mock(JwtService.class);

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter(jwtService);

    @Test
    void shouldReturnEmptyWhenAuthorizationHeaderIsMissing() {
        var request = MockServerHttpRequest.get("/api/v1/orders")
                .build();

        var exchange = MockServerWebExchange.from(request);

        var result = converter.convert(exchange).block();

        assertThat(result).isNull();

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldReturnEmptyWhenHeaderIsInvalid() {
        var request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Basic test")
                .build();

        var exchange = MockServerWebExchange.from(request);

        var result = converter.convert(exchange).block();

        assertThat(result).isNull();

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldReturnEmptyWhenTokenIsInvalid() {
        var request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();

        var exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        var result = converter.convert(exchange).block();

        assertThat(result).isNull();
    }

    @Test
    void shouldCreateAuthenticationWhenTokenIsValid() {
        var request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();

        var exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractSubject("valid-token"))
                .thenReturn("admin@test.com");

        when(jwtService.extractRole("valid-token"))
                .thenReturn("ADMIN");

        var authentication =
                (UsernamePasswordAuthenticationToken)
                        converter.convert(exchange).block();

        assertThat(authentication).isNotNull();

        assertThat(authentication.getName())
                .isEqualTo("admin@test.com");

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }
}