package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Users", description = "Các API quản lý người dùng")
public class UserController {

    private final UserService service;

    @PostMapping
    @Operation(summary = "Tạo người dùng mới")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody UserDto.UserCreateRequest body) {
        User user = User.builder()
                .email(body.email())
                .password(body.password())
                .fullName(body.fullName())
                .role(body.role())
                .build();

        User saved = service.create(user);
        UserDto.UserResponse res = mapToResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Người dùng tạo thành công", res));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả người dùng")
    public ResponseEntity<ApiResponse<?>> getAll() {
        List<User> users = service.findAll();
        List<UserDto.UserResponse> res = users.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách người dùng thành công", res));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy người dùng theo ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Integer id) {
        User user = service.findById(id);
        UserDto.UserResponse res = mapToResponse(user);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy người dùng thành công", res));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin người dùng")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Integer id, @Valid @RequestBody UserDto.UserUpdateRequest body) {
        User user = User.builder()
                .email(body.email())
                .password(body.password())
                .fullName(body.fullName())
                .role(body.role())
                .build();

        User updated = service.update(id, user);
        UserDto.UserResponse res = mapToResponse(updated);
        return ResponseEntity.ok(ApiResponse.success(200, "Người dùng cập nhật thành công", res));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Người dùng xóa thành công", null));
    }

    private UserDto.UserResponse mapToResponse(User user) {
        return new UserDto.UserResponse(
                user.getId(), 
                user.getEmail(), 
                user.getFullName(), 
                user.getRole()
        );
    }
}
