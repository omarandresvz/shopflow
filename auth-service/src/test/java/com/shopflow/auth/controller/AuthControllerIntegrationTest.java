package com.shopflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.auth.dto.request.LoginRequest;
import com.shopflow.auth.dto.request.RegisterRequest;
import com.shopflow.auth.entity.Role;
import com.shopflow.auth.entity.User;
import com.shopflow.auth.repository.UserRepository;
import com.shopflow.shared.security.model.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration=3600000"
})
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Juan",
                "Perez",
                "juan@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));

        assertThat(repository.existsByEmail("juan@test.com")).isTrue();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        repository.save(User.builder()
                .firstName("Ana")
                .lastName("Gomez")
                .email("ana@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build());

        RegisterRequest request = new RegisterRequest(
                "Ana",
                "Gomez",
                "ana@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        repository.save(User.builder()
                .firstName("Pedro")
                .lastName("Lopez")
                .email("pedro@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build());

        LoginRequest request = new LoginRequest(
                "pedro@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("pedro@test.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginCredentialsAreInvalid() throws Exception {
        repository.save(User.builder()
                .firstName("Maria")
                .lastName("Diaz")
                .email("maria@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build());

        LoginRequest request = new LoginRequest(
                "maria@test.com",
                "wrong-password"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCurrentUserProfile() throws Exception {
        User saved = repository.save(User.builder()
                .firstName("Carlos")
                .lastName("Rojas")
                .email("carlos@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build());

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(authentication(customerAuth(saved.getId(), saved.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("Rojas"))
                .andExpect(jsonPath("$.email").value("carlos@test.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldAllowAdminAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/auth/admin/test")
                        .with(authentication(adminAuth(1L, "admin@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Admin access granted"));
    }

    @Test
    void shouldReturnForbiddenWhenCustomerAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/auth/admin/test")
                        .with(authentication(customerAuth(1L, "customer@test.com"))))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken customerAuth(Long userId, String email) {
        CurrentUser currentUser = new CurrentUser(
                userId,
                email,
                "CUSTOMER"
        );

        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    private UsernamePasswordAuthenticationToken adminAuth(Long userId, String email) {
        CurrentUser currentUser = new CurrentUser(
                userId,
                email,
                "ADMIN"
        );

        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}