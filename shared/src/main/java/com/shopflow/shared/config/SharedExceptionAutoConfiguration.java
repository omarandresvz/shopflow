package com.shopflow.shared.config;

import com.shopflow.shared.exception.ErrorResponseFactory;
import com.shopflow.shared.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SharedExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ErrorResponseFactory errorResponseFactory() {
        return new ErrorResponseFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(
            ErrorResponseFactory errorResponseFactory
    ) {
        return new GlobalExceptionHandler(errorResponseFactory);
    }
}