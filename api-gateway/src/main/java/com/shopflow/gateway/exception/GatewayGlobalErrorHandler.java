package com.shopflow.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GatewayGlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> handle(
            @NonNull ServerWebExchange exchange,
            @NonNull Throwable ex
    ) {
        var response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.empty();
        }

        HttpStatus status = resolveStatus(ex);

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", resolveMessage(status),
                "code", resolveCode(status),
                "path", exchange.getRequest().getPath().value()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return response.writeWith(Mono.just(buffer));

        } catch (Exception e) {
            return Mono.<Void>error(e);
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }

        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    private String resolveMessage(HttpStatus status) {
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "Servicio temporalmente no disponible";
        }

        return "Error interno del gateway";
    }

    private String resolveCode(HttpStatus status) {
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "SERVICE_UNAVAILABLE";
        }

        return "GATEWAY_ERROR";
    }
}