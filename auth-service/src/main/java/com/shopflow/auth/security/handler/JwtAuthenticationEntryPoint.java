package com.shopflow.auth.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.auth.exception.ErrorCode;
import com.shopflow.auth.exception.ErrorResponse;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) {
        try {
            ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    401,
                    "Unauthorized",
                    "Authentication required",
                    ErrorCode.INTERNAL_SERVER_ERROR.name(),
                    request.getRequestURI(),
                    List.of()
            );

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            objectMapper.writeValue(response.getOutputStream(), error);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}