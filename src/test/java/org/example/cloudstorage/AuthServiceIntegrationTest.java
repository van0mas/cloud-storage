package org.example.cloudstorage;

import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.exception.UserAlreadyExistsException;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.example.cloudstorage.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class AuthServiceIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_shouldSaveUser() {
        AuthRequestDto dto = new AuthRequestDto("test", "password");

        AuthResponseDto response = authService.register(dto);

        assertNotNull(response);
        assertEquals("test", response.getUsername());

        User saved = userRepository.findByUsername("test").orElseThrow();
        assertTrue(passwordEncoder.matches("password", saved.getPassword()));
    }

    @Test
    void register_shouldThrowException_whenUsernameExists() {
        AuthRequestDto dto = new AuthRequestDto("duplicate", "password");
        authService.register(dto);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(dto));
    }

    @Test
    void login_shouldReturnDto_whenValidCredentials() {
        AuthRequestDto dto = new AuthRequestDto("user1", "pass");
        authService.register(dto);

        AuthResponseDto login = authService.login(new AuthRequestDto("user1", "pass"));
        assertEquals("user1", login.getUsername());
    }

    @Test
    void login_shouldThrowException_whenInvalidCredentials() {
        AuthRequestDto dto = new AuthRequestDto("user2", "pass");
        authService.register(dto);

        assertThrows(Exception.class,
                () -> authService.login(new AuthRequestDto("user2", "wrongpass")));
    }

    @Test
    void logout_shouldClearAuthentication() {
        AuthRequestDto dto = new AuthRequestDto("user3", "pass");
        authService.register(dto);

        authService.login(new AuthRequestDto("user3", "pass"));

        // Проверяем, что аутентификация установлена после login
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        // Вызываем logout
        authService.logout();

        // После logout контекст должен быть пустым
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

}

