package com.shopflow.auth.repository;

import com.shopflow.auth.entity.Role;
import com.shopflow.auth.entity.User;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserRepository repository;

    @Test
    void shouldReturnTrueWhenEmailExists() {

        User user = User.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .password("password")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        repository.save(user);

        boolean exists = repository.existsByEmail(
                "juan@test.com"
        );

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {

        boolean exists = repository.existsByEmail(
                "missing@test.com"
        );

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindUserByEmail() {

        User user = User.builder()
                .firstName("Ana")
                .lastName("Gomez")
                .email("ana@test.com")
                .password("password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        repository.save(user);

        var result = repository.findByEmail(
                "ana@test.com"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("ana@test.com");

        assertThat(result.get().getRole())
                .isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldReturnEmptyWhenUserEmailDoesNotExist() {

        var result = repository.findByEmail(
                "unknown@test.com"
        );

        assertThat(result).isEmpty();
    }
}