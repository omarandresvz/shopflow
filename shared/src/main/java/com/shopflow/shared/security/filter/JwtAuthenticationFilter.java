package com.shopflow.shared.security.filter;

import com.shopflow.shared.security.jwt.JwtService;
import com.shopflow.shared.security.model.CurrentUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String path = request.getServletPath();

            if (isPublicPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || authHeader.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!authHeader.startsWith("Bearer ")) {
                throw new BadCredentialsException("Invalid authorization header");
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            String email = jwtService.extractSubject(token);
            String role = jwtService.extractRole(token);

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

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

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        }
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/login")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/error");
    }
}