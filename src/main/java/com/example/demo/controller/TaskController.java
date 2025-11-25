package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.TaskDto;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Các API quản lý task")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepo;
    private final SecurityUtil securityUtil;

    private User getCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new RuntimeException("User chưa đăng nhập");
        }
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    private boolean isAdmin() {
        return securityUtil.isAdmin();
    }

    @PostMapping
    @Operation(summary = "Tạo task mới")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody TaskDto.TaskCreateRequest body) {
        User currentUser = getCurrentUser();
        
        Task task = Task.builder()
                .title(body.title())
                .description(body.description())
                .deadline(body.deadline())
                .build();
        
        if (body.status() != null) {
            task.setStatus(Task.Status.valueOf(body.status().toUpperCase()));
        }

        Task saved = taskService.create(task, currentUser);
        TaskDto.TaskResponse res = mapToResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Task tạo thành công", res));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách task với phân trang, sắp xếp và filter")
    public ResponseEntity<ApiResponse<?>> findAll(
            @RequestParam(name = "status", required = false) String status,
            Pageable pageable
    ) {
        User currentUser = getCurrentUser();
        boolean admin = isAdmin();
        
        Task.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Task.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "Status không hợp lệ. Chỉ chấp nhận: PENDING, IN_PROGRESS, COMPLETED"));
            }
        }
        
        Page<Task> taskPage;
        if (admin) {
            taskPage = taskService.findAll(pageable, statusEnum);
        } else {
            taskPage = taskService.findByUser(currentUser, pageable, statusEnum);
        }

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

    @GetMapping("/{id}")
    @Operation(summary = "Lấy task theo ID")
    public ResponseEntity<ApiResponse<?>> findById(@PathVariable Integer id) {
        User currentUser = getCurrentUser();
        boolean admin = isAdmin();
        
        Task task = taskService.findById(id, currentUser, admin);
        TaskDto.TaskResponse res = mapToResponse(task);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy task thành công", res));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật task")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Integer id,
                                                  @Valid @RequestBody TaskDto.TaskUpdateRequest body) {
        User currentUser = getCurrentUser();
        boolean admin = isAdmin();
        
        Task input = Task.builder()
                .title(body.title())
                .description(body.description())
                .deadline(body.deadline())
                .build();
        
        if (body.status() != null) {
            input.setStatus(Task.Status.valueOf(body.status().toUpperCase()));
        }

        Task updated = taskService.update(id, input, currentUser, admin);
        TaskDto.TaskResponse res = mapToResponse(updated);
        return ResponseEntity.ok(ApiResponse.success(200, "Task cập nhật thành công", res));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa task")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Integer id) {
        User currentUser = getCurrentUser();
        boolean admin = isAdmin();
        
        taskService.delete(id, currentUser, admin);
        return ResponseEntity.ok(ApiResponse.success(200, "Task xóa thành công", null));
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
}
