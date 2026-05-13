package com.shopflow.auth.controller;

import com.shopflow.auth.dto.request.LoginRequest;
import com.shopflow.auth.dto.request.RegisterRequest;
import com.shopflow.auth.dto.response.LoginResponse;
import com.shopflow.auth.dto.response.RegisterResponse;
import com.shopflow.auth.dto.response.UserProfileResponse;
import com.shopflow.auth.service.AuthService;
import com.shopflow.shared.security.model.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( value = "/api/v1/auth", produces = "application/json")
@RequiredArgsConstructor
@Tag(
        name = "Autenticación",
        description = "Endpoints para registro, login y perfil del usuario autenticado"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            description = "Registra un nuevo usuario en el sistema"
    )
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación")
    @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "400", description = "Error de validación")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Obtener perfil del usuario autenticado",
            description = "Retorna la información del usuario autenticado a partir del token JWT"
    )
    @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        UserProfileResponse response = authService.getCurrentUser(currentUser.email());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/test")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Probar acceso de administrador",
            description = "Endpoint de prueba disponible solo para usuarios con rol ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Acceso de administrador concedido")
    @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access granted");
    }
}