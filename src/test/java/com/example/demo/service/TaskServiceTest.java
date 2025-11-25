package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TaskService Tests")
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .password("encoded")
                .fullName("Test User")
                .role("USER")
                .build();

        testTask = Task.builder()
                .id(1)
                .title("Test Task")
                .description("Test Description")
                .status(Task.Status.PENDING)
                .deadline(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() {
        // Given
        Task newTask = Task.builder()
                .title("New Task")
                .description("New Description")
                .status(Task.Status.PENDING)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.create(newTask, testUser);

        // Then
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should find task by id - ADMIN can access any task")
    void testFindByIdAsAdmin() {
        // Given
        User adminUser = User.builder().id(2).email("admin@example.com").role("ADMIN").build();
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When
        Task result = taskService.findById(1, adminUser, true);

        // Then
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should find task by id - USER can access own task")
    void testFindByIdAsUserOwnTask() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When
        Task result = taskService.findById(1, testUser, false);

        // Then
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when USER tries to access other's task")
    void testFindByIdForbidden() {
        // Given
        User otherUser = User.builder().id(2).email("other@example.com").role("USER").build();
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThrows(ForbiddenException.class, () -> {
            taskService.findById(1, otherUser, false);
        });
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found")
    void testFindByIdNotFound() {
        // Given
        when(taskRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.findById(999, testUser, false);
        });
    }

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask() {
        // Given
        Task input = Task.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.Status.IN_PROGRESS)
                .build();

        Task updatedTask = Task.builder()
                .id(1)
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.Status.IN_PROGRESS)
                .user(testUser)
                .build();

        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        Task result = taskService.update(1, input, testUser, false);

        // Then
        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When
        taskService.delete(1, testUser, false);

        // Then
        verify(taskRepository, times(1)).deleteById(1);
    }
}
