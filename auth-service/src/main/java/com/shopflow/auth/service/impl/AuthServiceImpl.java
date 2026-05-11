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
import com.shopflow.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .email(normalizedEmail)
            .password(passwordEncoder.encode(request.password()))
            .role(Role.CUSTOMER)
            .enabled(true)
            .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            "Usuario registrado exitosamente"
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                normalizedEmail,
                    request.password()
            )
        );

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(UserNotFoundException::new);

        String token = jwtService.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
        );

        return new LoginResponse(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            token
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}