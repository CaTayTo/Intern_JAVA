package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AuthDto;
import com.example.demo.entity.User;
import com.example.demo.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Các API liên quan đến đăng ký, đăng nhập")
public class AuthController {

    private final UserAuthService service;

    /**
     * Đăng ký người dùng mới
     * - Truyền JSON: { "email": "user@gmail.com", "password": "password123", "role": "USER" }
     * - Trả về UserResponse (không có passwordHash)
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký người dùng mới")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        User saved = service.register(req);
        AuthDto.UserResponse userRes = new AuthDto.UserResponse(
                saved.getId(), 
                saved.getEmail(), 
                saved.getFullName(), 
                saved.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Đăng ký thành công", userRes));
    }

    /**
     * Đăng nhập, trả về JWT token
     * - Truyền JSON: { "email": "user@gmail.com", "password": "password123" }
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập và nhận JWT token")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        String token = service.login(req.email(), req.password());
        return ResponseEntity.ok(ApiResponse.success(200, "Đăng nhập thành công", new AuthDto.TokenResponse(token)));
    }
}
