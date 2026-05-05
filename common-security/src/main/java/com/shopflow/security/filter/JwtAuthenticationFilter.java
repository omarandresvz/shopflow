package com.shopflow.security.filter;

import com.shopflow.security.jwt.JwtService;
import com.shopflow.security.model.CurrentUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Leer header Authorization
        String authHeader = request.getHeader("Authorization");

        // Si no hay token, continuar sin autenticación
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer token
        String token = authHeader.substring(7);

        // Token inválido
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer datos del token
        String email = jwtService.extractSubject(token);
        String role = jwtService.extractRole(token);

        // 4. Crear authorities
        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        // 5. Crear Authentication
        /*var authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                authorities
        );
        */

        var currentUser = new CurrentUser(
                jwtService.extractUserId(token),
                email,
                role
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                authorities
        );

        // 6. Guardar en contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. Continuar request
        filterChain.doFilter(request, response);
    }
}