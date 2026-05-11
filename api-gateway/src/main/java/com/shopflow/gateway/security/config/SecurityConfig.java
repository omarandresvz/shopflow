package com.shopflow.gateway.security.config;

import com.shopflow.gateway.security.converter.JwtAuthenticationConverter;
import com.shopflow.gateway.security.handler.GatewayAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final GatewayAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/api/v1/auth/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )

                .addFilterAt(
                        (exchange, chain) -> jwtAuthenticationConverter.convert(exchange)
                                .flatMap(authentication -> {
                                    var context = SecurityContextHolder.createEmptyContext();
                                    context.setAuthentication(authentication);

                                    return chain.filter(exchange)
                                            .contextWrite(
                                                    ReactiveSecurityContextHolder.withSecurityContext(
                                                            Mono.just(context)
                                                    )
                                            );
                                })
                                .switchIfEmpty(chain.filter(exchange)),
                        SecurityWebFiltersOrder.AUTHENTICATION
                )

                .build();
    }
}