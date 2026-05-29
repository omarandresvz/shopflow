package com.shopflow.auth.config;

import com.shopflow.auth.entity.User;
import com.shopflow.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultUsersSeederTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private DefaultUsersSeeder seeder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        seeder = new DefaultUsersSeeder(userRepository, passwordEncoder);

        ReflectionTestUtils.setField(seeder, "adminEmail", "admin@shopflow.com");
        ReflectionTestUtils.setField(seeder, "adminPassword", "Admin123*");
        ReflectionTestUtils.setField(seeder, "customerEmail", "customer@shopflow.com");
        ReflectionTestUtils.setField(seeder, "customerPassword", "Customer123*");

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");
    }

    @Test
    void shouldCreateDefaultUsersWhenTheyDoNotExist() throws Exception {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        seeder.run();

        verify(userRepository, times(2)).save(any(User.class));
        verify(passwordEncoder, times(2)).encode(anyString());
    }

    @Test
    void shouldNotCreateUsersWhenTheyAlreadyExist() throws Exception {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldSkipUsersWhenEmailOrPasswordIsMissing() throws Exception {
        ReflectionTestUtils.setField(seeder, "adminEmail", "");
        ReflectionTestUtils.setField(seeder, "adminPassword", "");
        ReflectionTestUtils.setField(seeder, "customerEmail", "");
        ReflectionTestUtils.setField(seeder, "customerPassword", "");

        seeder.run();

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
}