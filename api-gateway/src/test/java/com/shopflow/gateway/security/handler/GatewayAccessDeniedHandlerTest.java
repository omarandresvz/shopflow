package com.shopflow.gateway.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private final GatewayAccessDeniedHandler handler =
            new GatewayAccessDeniedHandler(objectMapper);

    @SuppressWarnings("null")
    @Test
    void shouldReturnForbiddenResponse() {
        var exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/v1/products")
                        .build()
        );

        handler.handle(
                exchange,
                new AccessDeniedException("Access denied")
        ).block();

        var response = exchange.getResponse();

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");

        String body = response.getBodyAsString().block();

        assertThat(body).contains("\"status\":403");
        assertThat(body).contains("\"error\":\"Forbidden\"");
        assertThat(body).contains("\"message\":\"No tienes permisos para realizar esta operación\"");
        assertThat(body).contains("\"code\":\"ACCESS_DENIED\"");
        assertThat(body).contains("\"path\":\"/api/v1/products\"");
    }
}