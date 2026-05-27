package com.shopflow.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayGlobalErrorHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private final GatewayGlobalErrorHandler handler =
            new GatewayGlobalErrorHandler(objectMapper);

    @SuppressWarnings("null")
    @Test
    void shouldReturnServiceUnavailableWhenExceptionIsGeneric() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .get("/api/v1/products")
                        .build()
        );

        handler.handle(
                exchange,
                new RuntimeException("Connection refused")
        ).block();

        var response = exchange.getResponse();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");

        String body = response.getBodyAsString().block();

        assertThat(body).contains("\"status\":503");
        assertThat(body).contains("\"error\":\"Service Unavailable\"");
        assertThat(body).contains("\"message\":\"Servicio temporalmente no disponible\"");
        assertThat(body).contains("\"code\":\"SERVICE_UNAVAILABLE\"");
        assertThat(body).contains("\"path\":\"/api/v1/products\"");
    }

    @SuppressWarnings("null")
    @Test
    void shouldReturnGatewayErrorWhenExceptionHasHttpStatus() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .get("/api/v1/orders")
                        .build()
        );

        handler.handle(
                exchange,
                new ResponseStatusException(HttpStatus.BAD_GATEWAY)
        ).block();

        var response = exchange.getResponse();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");

        String body = response.getBodyAsString().block();

        assertThat(body).contains("\"status\":502");
        assertThat(body).contains("\"error\":\"Bad Gateway\"");
        assertThat(body).contains("\"message\":\"Error interno del gateway\"");
        assertThat(body).contains("\"code\":\"GATEWAY_ERROR\"");
        assertThat(body).contains("\"path\":\"/api/v1/orders\"");
    }
}