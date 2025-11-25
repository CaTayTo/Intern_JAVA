package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class TaskDto {
    private TaskDto() {}

    public record TaskCreateRequest(
            @NotBlank(message = "Title không được để trống")
            @Size(max = 200, message = "Title tối đa 200 ký tự")
            String title,

            @Size(max = 1000, message = "Description tối đa 1000 ký tự")
            String description,

            String status,  // PENDING, IN_PROGRESS, COMPLETED
            LocalDateTime deadline
    ) {}

    public record TaskUpdateRequest(
            @NotBlank(message = "Title không được để trống")
            @Size(max = 200, message = "Title tối đa 200 ký tự")
            String title,

            @Size(max = 1000, message = "Description tối đa 1000 ký tự")
            String description,

            String status,
            LocalDateTime deadline
    ) {}

    public record TaskResponse(
            Integer id,
            Integer userId,
            String title,
            String description,
            String status,
            LocalDateTime deadline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
