package com.shopflow.auth.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.auth.exception.ErrorCode;
import com.shopflow.auth.exception.ErrorResponse;
import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) {
        try {
            ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    403,
                    "Forbidden",
                    "Access denied",
                    ErrorCode.INTERNAL_SERVER_ERROR.name(),
                    request.getRequestURI(),
                    List.of()
            );

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            objectMapper.writeValue(response.getOutputStream(), error);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}