package com.shopflow.gateway.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private final GatewayAuthenticationEntryPoint entryPoint =
            new GatewayAuthenticationEntryPoint(objectMapper);

    @SuppressWarnings("null")
@Test
    void shouldReturnUnauthorizedResponse() {
        var exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/v1/orders")
                        .build()
        );

        entryPoint.commence(
                exchange,
                new BadCredentialsException("Invalid token")
        ).block();

        var response = exchange.getResponse();

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");

        String body = response.getBodyAsString().block();

        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("\"error\":\"Unauthorized\"");
        assertThat(body).contains("\"message\":\"No autenticado o token inválido\"");
        assertThat(body).contains("\"code\":\"UNAUTHORIZED\"");
        assertThat(body).contains("\"path\":\"/api/v1/orders\"");
    }
}