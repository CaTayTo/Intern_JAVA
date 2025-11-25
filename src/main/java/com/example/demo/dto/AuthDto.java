package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public record RegisterRequest(
            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            String email,

            @NotBlank(message = "Mật khẩu không được để trống")
            @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
            String password,

            @NotBlank(message = "Họ tên không được để trống")
            @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
            String fullName,

            @Pattern(regexp = "^(USER|ADMIN)$", message = "Role phải là USER hoặc ADMIN")
            String role
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            String email,

            @NotBlank(message = "Mật khẩu không được để trống")
            String password
    ) {}

    public record TokenResponse(String token) {}

    public record UserResponse(Integer id, String email, String fullName, String role) {}
}
