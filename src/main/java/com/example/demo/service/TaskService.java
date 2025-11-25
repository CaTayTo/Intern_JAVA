package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    @Transactional
    public Task create(Task task, User user) {
        task.setId(null);
        task.setUser(user);
        Task saved = taskRepo.save(task);
        log.info("Task created: id={}, userId={}", saved.getId(), user.getId());
        return saved;
    }

    public Page<Task> findAll(Pageable pageable, Task.Status status) {
        if (status != null) {
            return taskRepo.findByStatus(status, pageable);
        }
        return taskRepo.findAll(pageable);
    }

    public Page<Task> findByUser(User user, Pageable pageable, Task.Status status) {
        if (status != null) {
            return taskRepo.findByUserAndStatus(user, status, pageable);
        }
        return taskRepo.findByUser(user, pageable);
    }

    public Page<Task> findByUserId(Integer userId, Pageable pageable, Task.Status status) {
        if (status != null) {
            return taskRepo.findByUser_IdAndStatus(userId, status, pageable);
        }
        return taskRepo.findByUser_Id(userId, pageable);
    }

    public Task findById(Integer id, User currentUser, boolean isAdmin) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: id={}", id);
                    return new ResourceNotFoundException("Task không tồn tại với id: " + id);
                });

        if (!isAdmin && !task.getUser().getId().equals(currentUser.getId())) {
            log.warn("Access denied: user {} tried to access task {}", currentUser.getId(), id);
            throw new ForbiddenException("Bạn không có quyền truy cập task này");
        }

        return task;
    }

    @Transactional
    public Task update(Integer id, Task input, User currentUser, boolean isAdmin) {
        Task task = findById(id, currentUser, isAdmin);

        task.setTitle(input.getTitle());
        task.setDescription(input.getDescription());
        if (input.getStatus() != null) {
            task.setStatus(input.getStatus());
        }
        task.setDeadline(input.getDeadline());

        Task updated = taskRepo.save(task);
        log.info("Task updated: id={}", id);
        return updated;
    }

    @Transactional
    public void delete(Integer id, User currentUser, boolean isAdmin) {
        Task task = findById(id, currentUser, isAdmin);
        taskRepo.deleteById(id);
        log.info("Task deleted: id={}", id);
    }
}