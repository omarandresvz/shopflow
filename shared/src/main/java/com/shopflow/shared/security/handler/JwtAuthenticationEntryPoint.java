package com.shopflow.shared.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shared.exception.CommonErrorCode;
import com.shopflow.shared.exception.ErrorResponse;
import com.shopflow.shared.exception.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) {
        try {
            ErrorResponse error = errorResponseFactory.build(
                    HttpStatus.UNAUTHORIZED,
                    CommonErrorCode.UNAUTHORIZED,
                    request.getRequestURI()
            );

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(response.getOutputStream(), error);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}