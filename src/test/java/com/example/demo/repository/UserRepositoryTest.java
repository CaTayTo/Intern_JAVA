package com.example.demo.repository;

import com.example.demo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Integration test với MySQL database (test profile)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Tạo user test
        testUser = User.builder()
                .email("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void testSaveUser() {
        // When
        User saved = userRepository.save(testUser);

        // Then
        assertNotNull(saved.getId());
        assertEquals("test@example.com", saved.getEmail());
        assertEquals("Test User", saved.getFullName());
        assertEquals("USER", saved.getRole());
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Given
        userRepository.save(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("notfound@example.com");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should not allow duplicate email")
    void testDuplicateEmail() {
        // Given
        userRepository.save(testUser);

        // When - tạo user với email trùng
        User duplicate = User.builder()
                .email("test@example.com")
                .password("password")
                .fullName("Duplicate User")
                .role("USER")
                .build();

        // Then - sẽ throw exception khi save
        assertThrows(Exception.class, () -> {
            userRepository.save(duplicate);
        });
    }
}

