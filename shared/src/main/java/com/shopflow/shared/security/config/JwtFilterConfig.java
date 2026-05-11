package com.shopflow.shared.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shared.exception.ErrorResponseFactory;
import com.shopflow.shared.security.filter.JwtAuthenticationFilter;
import com.shopflow.shared.security.handler.JwtAccessDeniedHandler;
import com.shopflow.shared.security.handler.JwtAuthenticationEntryPoint;
import com.shopflow.shared.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class JwtFilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        return new JwtAuthenticationFilter(jwtService, authenticationEntryPoint);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
            ObjectMapper objectMapper,
            ErrorResponseFactory errorResponseFactory
    ) {
        return new JwtAuthenticationEntryPoint(objectMapper, errorResponseFactory);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(
            ObjectMapper objectMapper,
            ErrorResponseFactory errorResponseFactory
    ) {
        return new JwtAccessDeniedHandler(objectMapper, errorResponseFactory);
    }
}