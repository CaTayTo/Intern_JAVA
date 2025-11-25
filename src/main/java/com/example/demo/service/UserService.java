package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;

    @Transactional
    public User create(User user) {
        user.setId(null);
        User saved = repo.save(user);
        log.info("User created: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public User findById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", id);
                    return new ResourceNotFoundException("User không tồn tại với id: " + id);
                });
    }

    @Transactional
    public User update(Integer id, User input) {
        User u = findById(id);
        u.setEmail(input.getEmail());
        u.setPassword(input.getPassword());
        u.setFullName(input.getFullName());
        u.setRole(input.getRole());
        User updated = repo.save(u);
        log.info("User updated: id={}", id);
        return updated;
    }

    @Transactional
    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            log.warn("User not found for deletion: id={}", id);
            throw new ResourceNotFoundException("User không tồn tại với id: " + id);
        }
        repo.deleteById(id);
        log.info("User deleted: id={}", id);
    }

}
