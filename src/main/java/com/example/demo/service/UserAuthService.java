package com.example.demo.service;

import com.example.demo.dto.AuthDto.LoginRequest;
import com.example.demo.dto.AuthDto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository repo;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder;

    public User register(RegisterRequest input) {
        if (repo.existsByEmail(input.email())) {
            log.warn("Email already exists: {}", input.email());
            throw new DuplicateException("Email đã tồn tại: " + input.email());
        }

        User u = User.builder()
                .email(input.email())
                .password(encoder.encode(input.password()))
                .fullName(input.fullName())
                .role(input.role() == null || input.role().isEmpty() ? "USER" : input.role())
                .build();

        User saved = repo.save(u);
        log.info("User registered successfully: {}", saved.getEmail());
        return saved;
    }

    public String login(String email, String rawPassword) {
        var u = repo.findByEmail(email).orElseThrow(() -> {
            log.warn("Login failed: email not found: {}", email);
            return new UnauthorizedException("Email không tồn tại");
        });

        if (!encoder.matches(rawPassword, u.getPassword())) {
            log.warn("Login failed: wrong password for email: {}", email);
            throw new UnauthorizedException("Mật khẩu không đúng");
        }

        String token = jwt.generateToken(u.getEmail(), u.getId());
        log.info("User logged in successfully: {}", u.getEmail());
        return token;
    }

}
