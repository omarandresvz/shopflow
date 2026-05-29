package com.shopflow.auth.config;

import com.shopflow.auth.entity.Role;
import com.shopflow.auth.entity.User;
import com.shopflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "app.default-users.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class DefaultUsersSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-users.admin.email:}")
    private String adminEmail;

    @Value("${app.default-users.admin.password:}")
    private String adminPassword;

    @Value("${app.default-users.customer.email:}")
    private String customerEmail;

    @Value("${app.default-users.customer.password:}")
    private String customerPassword;

    @Override
    public void run(String... args) {

        log.info("DefaultUsersSeeder ejecutándose...");
        
        createUserIfConfigured(
                adminEmail,
                adminPassword,
                "Admin",
                "ShopFlow",
                Role.ADMIN
        );

        createUserIfConfigured(
                customerEmail,
                customerPassword,
                "Customer",
                "ShopFlow",
                Role.CUSTOMER
        );
    }

    private void createUserIfConfigured(
            String email,
            String password,
            String firstName,
            String lastName,
            Role role
    ) {
        if (isBlank(email) || isBlank(password)) {
            log.info("Default {} user seeder skipped: email or password not configured.", role);
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Default {} user already exists: {}", role, normalizedEmail);
            return;
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);

        log.info("Default {} user created: {}", role, normalizedEmail);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
