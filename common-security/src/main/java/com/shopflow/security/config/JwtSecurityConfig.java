package com.shopflow.security.config;

import com.shopflow.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtSecurityConfig {

    @Bean
    public JwtService jwtService() {
        return new JwtService();
    }
}