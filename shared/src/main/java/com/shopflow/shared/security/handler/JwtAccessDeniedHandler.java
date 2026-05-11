package com.shopflow.shared.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shared.exception.CommonErrorCode;
import com.shopflow.shared.exception.ErrorResponse;
import com.shopflow.shared.exception.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) {
        try {
            ErrorResponse error = errorResponseFactory.build(
                    HttpStatus.FORBIDDEN,
                    CommonErrorCode.ACCESS_DENIED,
                    request.getRequestURI()
            );

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(response.getOutputStream(), error);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}