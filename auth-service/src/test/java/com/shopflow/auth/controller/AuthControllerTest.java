package com.shopflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.auth.dto.request.LoginRequest;
import com.shopflow.auth.dto.request.RegisterRequest;
import com.shopflow.auth.dto.response.LoginResponse;
import com.shopflow.auth.dto.response.RegisterResponse;
import com.shopflow.auth.dto.response.UserProfileResponse;
import com.shopflow.auth.service.AuthService;
import com.shopflow.shared.exception.ErrorResponseFactory;
import com.shopflow.shared.security.filter.JwtAuthenticationFilter;
import com.shopflow.shared.security.model.CurrentUser;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthController controller;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private AccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private ErrorResponseFactory errorResponseFactory;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Juan",
                "Perez",
                "juan@test.com",
                "Password123"
        );

        RegisterResponse response = new RegisterResponse(
                1L,
                "juan@test.com",
                "Usuario registrado exitosamente"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest(
                "juan@test.com",
                "Password123"
        );

        LoginResponse response = new LoginResponse(
                1L,
                "juan@test.com",
                "CUSTOMER",
                "jwt-token"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void shouldGetCurrentUserProfile() {
        CurrentUser currentUser = new CurrentUser(
                1L,
                "juan@test.com",
                "CUSTOMER"
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of()
        );

        UserProfileResponse response = new UserProfileResponse(
                1L,
                "Juan",
                "Perez",
                "juan@test.com",
                "CUSTOMER"
        );

        when(authService.getCurrentUser("juan@test.com")).thenReturn(response);

        ResponseEntity<UserProfileResponse> result = controller.me(authentication);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        assertThat(result.getBody().firstName()).isEqualTo("Juan");
        assertThat(result.getBody().lastName()).isEqualTo("Perez");
        assertThat(result.getBody().email()).isEqualTo("juan@test.com");
        assertThat(result.getBody().role()).isEqualTo("CUSTOMER");

        verify(authService).getCurrentUser("juan@test.com");
    }

    @Test
    void shouldAccessAdminTestEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/auth/admin/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin access granted"));
    }
}