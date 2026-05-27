package com.shopflow.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void shouldCreateBusinessExceptionWithErrorCodeAndStatus() {
        TestBusinessException exception = new TestBusinessException();

        assertThat(exception.getMessage()).isEqualTo("Solicitud inválida");
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_REQUEST);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateBusinessExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Remote service error");

        TestBusinessException exception = new TestBusinessException(cause);

        assertThat(exception.getMessage()).isEqualTo("Error interno del servidor");
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INTERNAL_ERROR);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    private static class TestBusinessException extends BusinessException {

        TestBusinessException() {
            super(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        TestBusinessException(Throwable cause) {
            super(CommonErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, cause);
        }
    }
}