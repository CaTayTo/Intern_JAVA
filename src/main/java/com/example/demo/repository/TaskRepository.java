package com.example.demo.repository;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    Page<Task> findAll(Pageable pageable);

    List<Task> findByUser(User user);

    Page<Task> findByUser(User user, Pageable pageable);

    List<Task> findByUser_Id(Integer userId);

    Page<Task> findByUser_Id(Integer userId, Pageable pageable);

    Page<Task> findByStatus(Task.Status status, Pageable pageable);

    Page<Task> findByUserAndStatus(User user, Task.Status status, Pageable pageable);

    Page<Task> findByUser_IdAndStatus(Integer userId, Task.Status status, Pageable pageable);
}
