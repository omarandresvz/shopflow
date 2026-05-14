package com.shopflow.auth.service.impl;

import com.shopflow.auth.dto.request.LoginRequest;
import com.shopflow.auth.dto.request.RegisterRequest;
import com.shopflow.auth.dto.response.LoginResponse;
import com.shopflow.auth.dto.response.RegisterResponse;
import com.shopflow.auth.dto.response.UserProfileResponse;
import com.shopflow.auth.entity.Role;
import com.shopflow.auth.entity.User;
import com.shopflow.auth.exception.custom.EmailAlreadyExistsException;
import com.shopflow.auth.exception.custom.UserNotFoundException;
import com.shopflow.auth.repository.UserRepository;
import com.shopflow.shared.security.jwt.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl service;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest(
                "Juan",
                "Pérez",
                "juan@test.com",
                "123456"
        );

        loginRequest = new LoginRequest(
                "juan@test.com",
                "123456"
        );

        user = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = service.register(registerRequest);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.message()).isEqualTo("Usuario registrado exitosamente");

        verify(userRepository).existsByEmail("juan@test.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository).existsByEmail("juan@test.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "juan@test.com", "CUSTOMER"))
                .thenReturn("jwt-token");

        LoginResponse response = service.login(loginRequest);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        assertThat(response.token()).isEqualTo("jwt-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("juan@test.com");
        verify(jwtService).generateToken(1L, "juan@test.com", "CUSTOMER");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundAfterLoginAuthentication() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("juan@test.com");
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Credenciales inválidas") {});

        assertThatThrownBy(() -> service.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void shouldGetCurrentUserSuccessfully() {
        when(userRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(user));

        UserProfileResponse response = service.getCurrentUser("juan@test.com");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Juan");
        assertThat(response.lastName()).isEqualTo("Pérez");
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");

        verify(userRepository).findByEmail("juan@test.com");
    }

    @Test
    void shouldThrowExceptionWhenCurrentUserNotFound() {
        when(userRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser("juan@test.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail("juan@test.com");
    }

}