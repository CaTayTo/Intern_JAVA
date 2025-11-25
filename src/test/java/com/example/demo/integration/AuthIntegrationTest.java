package com.example.demo.integration;

import com.example.demo.dto.AuthDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

// Integration test với MySQL database và Spring context (test profile)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register and login successfully")
    void testRegisterAndLogin() {
        // Given - Đăng ký user mới
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest(
                "newuser@example.com",
                "password123",
                "Nguyen Van A",
                "USER"
        );

        // When - Đăng ký
        User registered = userAuthService.register(registerRequest);

        // Then
        assertNotNull(registered.getId());
        assertEquals("newuser@example.com", registered.getEmail());
        assertEquals("Nguyen Van A", registered.getFullName());
        assertTrue(passwordEncoder.matches("password123", registered.getPassword()));

        // When - Đăng nhập
        String token = userAuthService.login("newuser@example.com", "password123");

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should not allow duplicate email registration")
    void testDuplicateEmailRegistration() {
        // Given - Đăng ký user đầu tiên
        AuthDto.RegisterRequest request1 = new AuthDto.RegisterRequest(
                "duplicate@example.com",
                "password123",
                "User 1",
                "USER"
        );
        userAuthService.register(request1);

        // When & Then - Đăng ký user thứ 2 với email trùng
        AuthDto.RegisterRequest request2 = new AuthDto.RegisterRequest(
                "duplicate@example.com",
                "password456",
                "User 2",
                "USER"
        );

        assertThrows(Exception.class, () -> {
            userAuthService.register(request2);
        });
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void testLoginWithWrongPassword() {
        // Given - Đăng ký user
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest(
                "user@example.com",
                "correct_password",
                "Test User",
                "USER"
        );
        userAuthService.register(registerRequest);

        // When & Then - Đăng nhập với password sai
        assertThrows(Exception.class, () -> {
            userAuthService.login("user@example.com", "wrong_password");
        });
    }

    @Test
    @DisplayName("Should fail login with non-existent email")
    void testLoginWithNonExistentEmail() {
        // When & Then - Đăng nhập với email không tồn tại
        assertThrows(Exception.class, () -> {
            userAuthService.login("notfound@example.com", "password123");
        });
    }
}

