package com.shopflow.shared.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shared.exception.CommonErrorCode;
import com.shopflow.shared.exception.ErrorResponseFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules();

    private final ErrorResponseFactory errorResponseFactory = new ErrorResponseFactory();

    private final JwtAccessDeniedHandler handler =
            new JwtAccessDeniedHandler(objectMapper, errorResponseFactory);

    @Test
    void shouldReturnForbiddenErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");

        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                request,
                response,
                new AccessDeniedException("Access denied")
        );

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        String body = response.getContentAsString();

        assertThat(body).contains("\"status\":403");
        assertThat(body).contains("\"error\":\"Forbidden\"");
        assertThat(body).contains("\"message\":\"" + CommonErrorCode.ACCESS_DENIED.getMessage() + "\"");
        assertThat(body).contains("\"code\":\"ACCESS_DENIED\"");
        assertThat(body).contains("\"path\":\"/api/v1/products\"");
    }
}