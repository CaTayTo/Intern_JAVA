package com.example.demo.repository;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Integration test với MySQL database (test profile)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository Integration Tests")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Tạo user test
        testUser = User.builder()
                .email("test@example.com")
                .password("encoded")
                .fullName("Test User")
                .role("USER")
                .build();
        testUser = userRepository.save(testUser);

        // Tạo task test
        testTask = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.Status.PENDING)
                .deadline(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("Should save task successfully")
    void testSaveTask() {
        // When
        Task saved = taskRepository.save(testTask);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test Task", saved.getTitle());
        assertEquals(testUser.getId(), saved.getUser().getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Should find tasks by user")
    void testFindByUser() {
        // Given
        taskRepository.save(testTask);

        // Tạo task thứ 2 cho cùng user
        Task task2 = Task.builder()
                .title("Task 2")
                .status(Task.Status.IN_PROGRESS)
                .user(testUser)
                .build();
        taskRepository.save(task2);

        // When
        List<Task> tasks = taskRepository.findByUser(testUser);

        // Then
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Test Task")));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 2")));
    }

    @Test
    @DisplayName("Should find tasks by user with pagination")
    void testFindByUserWithPagination() {
        // Given - tạo 5 tasks
        for (int i = 1; i <= 5; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .status(Task.Status.PENDING)
                    .user(testUser)
                    .build();
            taskRepository.save(task);
        }

        // When
        Pageable pageable = PageRequest.of(0, 2); // Page 0, size 2
        Page<Task> page = taskRepository.findByUser(testUser, pageable);

        // Then
        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @DisplayName("Should filter tasks by status")
    void testFindByStatus() {
        // Given
        taskRepository.save(testTask);

        Task completedTask = Task.builder()
                .title("Completed Task")
                .status(Task.Status.COMPLETED)
                .user(testUser)
                .build();
        taskRepository.save(completedTask);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> pendingTasks = taskRepository.findByStatus(Task.Status.PENDING, pageable);
        Page<Task> completedTasks = taskRepository.findByStatus(Task.Status.COMPLETED, pageable);

        // Then
        assertEquals(1, pendingTasks.getTotalElements());
        assertEquals(1, completedTasks.getTotalElements());
    }

    @Test
    @DisplayName("Should find tasks by user and status")
    void testFindByUserAndStatus() {
        // Given
        taskRepository.save(testTask);

        Task completedTask = Task.builder()
                .title("Completed Task")
                .status(Task.Status.COMPLETED)
                .user(testUser)
                .build();
        taskRepository.save(completedTask);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> pendingTasks = taskRepository.findByUserAndStatus(testUser, Task.Status.PENDING, pageable);

        // Then
        assertEquals(1, pendingTasks.getTotalElements());
        assertEquals("Test Task", pendingTasks.getContent().get(0).getTitle());
    }
}

