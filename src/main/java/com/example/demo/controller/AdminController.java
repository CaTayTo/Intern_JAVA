package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.TaskDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Các API dành cho quản trị viên")
public class AdminController {

    private final TaskService taskService;
    private final UserService userService;
    private final UserRepository userRepo;
    private final SecurityUtil securityUtil;

    private void checkAdmin() {
        if (!securityUtil.isAdmin()) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền truy cập");
        }
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả task của mọi user (chỉ ADMIN)")
    public ResponseEntity<ApiResponse<?>> getAllTasks(
            @RequestParam(name = "status", required = false) String status,
            Pageable pageable
    ) {
        checkAdmin();
        
        Task.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Task.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "Status không hợp lệ. Chỉ chấp nhận: PENDING, IN_PROGRESS, COMPLETED"));
            }
        }
        
        Page<Task> taskPage = taskService.findAll(pageable, statusEnum);
        
        List<TaskDto.TaskResponse> res = taskPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        
        String message = String.format(
                "Lấy danh sách task thành công (trang %d/%d, tổng %d task%s)",
                pageable.getPageNumber() + 1,
                taskPage.getTotalPages(),
                taskPage.getTotalElements(),
                statusEnum != null ? ", status: " + statusEnum : ""
        );
        
        return ResponseEntity.ok(ApiResponse.success(200, message, res));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả user (chỉ ADMIN)")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        checkAdmin();
        
        List<User> users = userService.findAll();
        List<UserDto.UserResponse> res = users.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách user thành công", res));
    }

    private TaskDto.TaskResponse mapToResponse(Task task) {
        return new TaskDto.TaskResponse(
                task.getId(),
                task.getUser().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getDeadline(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
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
