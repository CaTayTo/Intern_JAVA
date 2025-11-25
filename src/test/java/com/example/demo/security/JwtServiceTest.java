package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
@TestPropertySource(properties = "spring.security.jwt.secret=test-secret-key-for-testing-purposes-32-chars")
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        try {
            java.lang.reflect.Field secretField = JwtService.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtService, "test-secret-key-for-testing-purposes-32-chars");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        jwtService.init();
    }

    @Test
    @DisplayName("Should generate valid token")
    void testGenerateToken() {
        // Given
        String email = "test@example.com";
        Integer userId = 1;

        // When
        String token = jwtService.generateToken(email, userId);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract email from token")
    void testEmailExtraction() {
        // Given
        String email = "test@example.com";
        Integer userId = 1;
        String token = jwtService.generateToken(email, userId);

        // When
        String extractedEmail = jwtService.email(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract userId from token")
    void testUserIdExtraction() {
        // Given
        String email = "test@example.com";
        Integer userId = 1;
        String token = jwtService.generateToken(email, userId);

        // When
        Integer extractedUserId = jwtService.userId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidToken() {
        // Given
        String token = jwtService.generateToken("test@example.com", 1);

        // When
        boolean isValid = jwtService.valid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate corrupted token")
    void testInvalidToken() {
        // Given
        String corruptedToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.valid(corruptedToken);

        // Then
        assertFalse(isValid);
    }
}
