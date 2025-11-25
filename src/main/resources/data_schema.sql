-- Tạo database
USE task_management;

-- Bảng users: id, email, password, full_name, role, created_at, updated_at
CREATE TABLE IF NOT EXISTS users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password      VARCHAR(100) NOT NULL,
  full_name     VARCHAR(100) NOT NULL,
  role          VARCHAR(20) NOT NULL DEFAULT 'USER',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng tasks: id, title, description, status, deadline, user_id, created_at, updated_at
CREATE TABLE IF NOT EXISTS tasks (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(200) NOT NULL,
  description VARCHAR(1000) NULL,
  status      ENUM('PENDING','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PENDING',
  deadline    DATETIME NULL,
  user_id     INT NOT NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Dữ liệu mẫu: users
-- Password mẫu: "password123" (đã mã hóa bằng BCrypt)
INSERT INTO users (email, password, full_name, role) VALUES
('admin@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', 'ADMIN'),
('user1@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyen Van A', 'USER'),
('user2@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Tran Thi B', 'USER');

-- Dữ liệu mẫu: tasks
INSERT INTO tasks (user_id, title, description, status, deadline) VALUES
(1, 'Quản trị hệ thống', 'Quản lý và bảo trì hệ thống Task Management', 'IN_PROGRESS', '2025-12-01 17:00:00'),
(2, 'Học Spring Boot cơ bản', 'Học các khái niệm cơ bản về Spring Boot', 'PENDING', '2025-11-01 18:00:00'),
(2, 'Làm project Task Management', 'Hoàn thiện dự án Task Management theo yêu cầu', 'IN_PROGRESS', '2025-11-15 23:59:00'),
(3, 'Ôn Java OOP', 'Ôn tập các khái niệm OOP trong Java', 'COMPLETED', '2025-09-30 12:00:00'),
(3, 'Viết tài liệu API', 'Viết tài liệu hướng dẫn sử dụng API', 'PENDING', '2025-10-30 20:00:00');
