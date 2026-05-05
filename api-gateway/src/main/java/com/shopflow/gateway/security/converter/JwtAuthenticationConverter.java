package com.shopflow.gateway.security.converter;

import com.shopflow.gateway.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.core.Authentication;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtService jwtService;

   @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.empty();
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return Mono.empty();
        }

        String email = jwtService.extractSubject(token);
        String role = jwtService.extractRole(token);

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        return Mono.just(
                new UsernamePasswordAuthenticationToken(email, token, authorities)
        );
    }
}