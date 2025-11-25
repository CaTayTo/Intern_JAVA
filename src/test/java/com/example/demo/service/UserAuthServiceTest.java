package com.example.demo.service;

import com.example.demo.dto.AuthDto.LoginRequest;
import com.example.demo.dto.AuthDto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("UserAuthService Tests")
@ExtendWith(MockitoExtension.class)
public class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAuthService userAuthService;

    @BeforeEach
    void setUp() {
        // Setup mocks nếu cần
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterSuccess() {
        // Given
        RegisterRequest request = new RegisterRequest("newuser@gmail.com", "password123", "Nguyen Van A", "USER");
        User savedUser = User.builder()
                .id(1)
                .email("newuser@gmail.com")
                .password("encoded_password")
                .fullName("Nguyen Van A")
                .role("USER")
                .build();

        when(userRepository.existsByEmail("newuser@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userAuthService.register(request);

        // Then
        assertNotNull(result);
        assertEquals("newuser@gmail.com", result.getEmail());
        assertEquals("Nguyen Van A", result.getFullName());
        assertEquals("USER", result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateException when email exists")
    void testRegisterDuplicateEmail() {
        // Given
        RegisterRequest request = new RegisterRequest("existing@gmail.com", "password123", "Test User", "USER");

        when(userRepository.existsByEmail("existing@gmail.com")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateException.class, () -> {
            userAuthService.register(request);
        });
    }

    @Test
    @DisplayName("Should login successfully and return token")
    void testLoginSuccess() {
        // Given
        String email = "user@gmail.com";
        String rawPassword = "password123";
        String encodedPassword = "encoded_password";
        String token = "jwt_token_here";

        User user = User.builder()
                .id(1)
                .email(email)
                .password(encodedPassword)
                .fullName("Test User")
                .role("USER")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(email, 1)).thenReturn(token);

        // When
        String result = userAuthService.login(email, rawPassword);

        // Then
        assertEquals(token, result);
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtService, times(1)).generateToken(email, 1);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when email not found")
    void testLoginEmailNotFound() {
        // Given
        String email = "notfound@gmail.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            userAuthService.login(email, password);
        });
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when password wrong")
    void testLoginWrongPassword() {
        // Given
        String email = "user@gmail.com";
        String rawPassword = "wrong_password";
        String correctPassword = "correct_password";

        User user = User.builder()
                .id(1)
                .email(email)
                .password(correctPassword)
                .fullName("Test User")
                .role("USER")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, correctPassword)).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            userAuthService.login(email, rawPassword);
        });
    }
}
