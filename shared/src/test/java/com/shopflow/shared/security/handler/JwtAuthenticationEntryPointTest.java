package com.shopflow.shared.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shared.exception.CommonErrorCode;
import com.shopflow.shared.exception.ErrorResponseFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules();

    private final ErrorResponseFactory errorResponseFactory = new ErrorResponseFactory();

    private final JwtAuthenticationEntryPoint entryPoint =
            new JwtAuthenticationEntryPoint(objectMapper, errorResponseFactory);

    @Test
    void shouldReturnUnauthorizedErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/orders");

        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid token")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        String body = response.getContentAsString();

        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("\"error\":\"Unauthorized\"");
        assertThat(body).contains("\"message\":\"" + CommonErrorCode.UNAUTHORIZED.getMessage() + "\"");
        assertThat(body).contains("\"code\":\"UNAUTHORIZED\"");
        assertThat(body).contains("\"path\":\"/api/v1/orders\"");
    }
}