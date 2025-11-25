# 📊 Báo Cáo Tổng Hợp - Task Management System

## 1. Tổng Quan Hệ Thống

### 1.1. Mô tả
Hệ thống quản lý task (Task Management System) là ứng dụng web RESTful API cho phép người dùng quản lý công việc cá nhân với các tính năng:
- Đăng ký/Đăng nhập với JWT Authentication
- Quản lý task (CRUD) với phân quyền USER/ADMIN
- Phân trang, sắp xếp, lọc task
- Swagger UI để test API

### 1.2. Tech Stack
- **Backend**: Java 17, Spring Boot 3.5.6
- **Database**: MySQL 8.4
- **Security**: Spring Security + JWT (jjwt 0.11.5)
- **ORM**: Spring Data JPA / Hibernate
- **Documentation**: Swagger/OpenAPI 2.8.13
- **Build Tool**: Maven

---

## 2. Kiến Trúc Hệ Thống

### 2.1. Kiến trúc phân lớp (Layered Architecture)

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ AuthController│  │TaskController│  │AdminController│  │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │        GlobalExceptionHandler (@RestControllerAdvice)│
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Business Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │UserAuthService│  │ TaskService │  │ UserService  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │UserRepository│  │TaskRepository│                     │
│  └──────────────┘  └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                    Database (MySQL)                      │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │    users     │  │    tasks     │                     │
│  └──────────────┘  └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
```

### 2.2. Security Layer

```
┌─────────────────────────────────────────────────────────┐
│                    Security Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ SecurityConfig│  │ JwtAuthFilter│  │  JwtService  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │         BCryptPasswordEncoder                    │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 2.3. Luồng xử lý request

```
Client Request
    ↓
Security Filter (JwtAuthFilter)
    ↓
Controller Layer
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database
    ↓
Response (ApiResponse<T>)
```

---

## 3. Mô Hình Dữ Liệu

### 3.1. Sơ đồ ERD (Entity Relationship Diagram)

```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ PK  id              │
│     email (UNIQUE)  │
│     password        │
│     full_name       │
│     role            │
│     created_at      │
│     updated_at      │
└─────────────────────┘
         │
         │ 1
         │
         │ N
         │
┌─────────────────────┐
│       tasks         │
├─────────────────────┤
│ PK  id              │
│ FK  user_id ────────┘
│     title           │
│     description     │
│     status          │
│     deadline        │
│     created_at      │
│     updated_at      │
└─────────────────────┘
```

### 3.2. Bảng `users`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID người dùng |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE | Email đăng nhập |
| `password` | VARCHAR(100) | NOT NULL | Mật khẩu đã mã hóa (BCrypt) |
| `full_name` | VARCHAR(100) | NOT NULL | Họ và tên |
| `role` | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | Vai trò: USER hoặc ADMIN |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Thời gian cập nhật |

**Ràng buộc:**
- Email phải unique
- Role chỉ nhận giá trị: `USER` hoặc `ADMIN`

### 3.3. Bảng `tasks`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID task |
| `user_id` | INT | NOT NULL, FOREIGN KEY | ID người dùng sở hữu task |
| `title` | VARCHAR(200) | NOT NULL | Tiêu đề task |
| `description` | VARCHAR(1000) | NULL | Mô tả chi tiết |
| `status` | ENUM | NOT NULL, DEFAULT 'PENDING' | Trạng thái: PENDING, IN_PROGRESS, COMPLETED |
| `deadline` | DATETIME | NULL | Hạn chót |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Thời gian cập nhật |

**Ràng buộc:**
- Foreign Key: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- Status chỉ nhận: `PENDING`, `IN_PROGRESS`, `COMPLETED`

### 3.4. Quan hệ giữa các bảng

- **users (1) ──< tasks (N)**: Một user có nhiều tasks (One-to-Many)
- **Cascade Delete**: Khi xóa user, tất cả tasks của user đó cũng bị xóa

---

## 4. Luồng API

### 4.1. Luồng Authentication

#### 4.1.1. Đăng ký (Register)

```
Client
  │
  │ POST /api/auth/register
  │ {
  │   "email": "user@example.com",
  │   "password": "password123",
  │   "fullName": "Nguyen Van A",
  │   "role": "USER"
  │ }
  ↓
AuthController
  ↓
UserAuthService
  │ - Kiểm tra email đã tồn tại?
  │ - Mã hóa password (BCrypt)
  │ - Tạo user mới với role USER
  ↓
UserRepository.save()
  ↓
Database (users table)
  ↓
Response 201 Created
{
  "code": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "role": "USER"
  }
}
```

#### 4.1.2. Đăng nhập (Login)

```
Client
  │
  │ POST /api/auth/login
  │ {
  │   "email": "user@example.com",
  │   "password": "password123"
  │ }
  ↓
AuthController
  ↓
UserAuthService
  │ - Tìm user theo email
  │ - So sánh password (BCrypt.matches)
  │ - Tạo JWT token (chứa email, userId)
  ↓
JwtService.generateToken()
  ↓
Response 200 OK
{
  "code": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 4.2. Luồng CRUD Task

#### 4.2.1. Tạo Task (Create)

```
Client
  │
  │ POST /api/tasks
  │ Authorization: Bearer <JWT_TOKEN>
  │ {
  │   "title": "Học Spring Boot",
  │   "description": "Học các khái niệm cơ bản",
  │   "status": "PENDING",
  │   "deadline": "2025-12-01T17:00:00"
  │ }
  ↓
JwtAuthFilter
  │ - Extract token từ header
  │ - Validate token
  │ - Set Authentication vào SecurityContext
  ↓
TaskController
  │ - Lấy current user từ SecurityContext
  │ - Tạo Task entity
  │ - Gán user cho task
  ↓
TaskService.create()
  ↓
TaskRepository.save()
  ↓
Database (tasks table)
  ↓
Response 201 Created
{
  "code": 201,
  "message": "Task tạo thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "title": "Học Spring Boot",
    "description": "Học các khái niệm cơ bản",
    "status": "PENDING",
    "deadline": "2025-12-01T17:00:00",
    "createdAt": "2025-11-11T10:00:00",
    "updatedAt": "2025-11-11T10:00:00"
  }
}
```

#### 4.2.2. Lấy danh sách Task (Read All)

```
Client
  │
  │ GET /api/tasks?page=0&size=10&sort=createdAt,desc&status=PENDING
  │ Authorization: Bearer <JWT_TOKEN>
  ↓
JwtAuthFilter
  ↓
TaskController
  │ - Lấy current user và role
  │ - Parse query params (page, size, sort, status)
  ↓
TaskService
  │ - Nếu ADMIN: findAll(status, pageable)
  │ - Nếu USER: findByUser(user, status, pageable)
  ↓
TaskRepository
  │ - Query với pagination, sorting, filtering
  ↓
Database
  ↓
Response 200 OK
{
  "code": 200,
  "message": "Lấy danh sách task thành công (trang 1/2, tổng 15 tasks)",
  "data": [
    { "id": 1, "title": "Task 1", ... },
    { "id": 2, "title": "Task 2", ... }
  ]
}
```

#### 4.2.3. Lấy Task theo ID (Read One)

```
Client
  │
  │ GET /api/tasks/{id}
  │ Authorization: Bearer <JWT_TOKEN>
  ↓
TaskController
  ↓
TaskService.findById()
  │ - Tìm task theo ID
  │ - Kiểm tra quyền:
  │   * ADMIN: có quyền xem tất cả
  │   * USER: chỉ xem task của mình
  │   * Nếu không có quyền → 403 Forbidden
  ↓
Response 200 OK hoặc 403 Forbidden
```

#### 4.2.4. Cập nhật Task (Update)

```
Client
  │
  │ PUT /api/tasks/{id}
  │ Authorization: Bearer <JWT_TOKEN>
  │ {
  │   "title": "Học Spring Boot Advanced",
  │   "status": "IN_PROGRESS"
  │ }
  ↓
TaskController
  ↓
TaskService.update()
  │ - Tìm task và kiểm tra quyền
  │ - Cập nhật các field
  │ - Tự động set updatedAt
  ↓
TaskRepository.save()
  ↓
Response 200 OK
```

#### 4.2.5. Xóa Task (Delete)

```
Client
  │
  │ DELETE /api/tasks/{id}
  │ Authorization: Bearer <JWT_TOKEN>
  ↓
TaskController
  ↓
TaskService.delete()
  │ - Tìm task và kiểm tra quyền
  │ - Xóa task
  ↓
TaskRepository.deleteById()
  ↓
Response 200 OK
```

### 4.3. Luồng Admin APIs

```
Client (ADMIN)
  │
  │ GET /api/admin/tasks?page=0&size=10&status=PENDING
  │ Authorization: Bearer <ADMIN_JWT_TOKEN>
  ↓
SecurityConfig
  │ - Kiểm tra role ADMIN (@PreAuthorize)
  ↓
AdminController
  │ - checkAdmin()
  │ - Lấy tất cả tasks (không filter theo user)
  ↓
TaskService.findAll()
  ↓
Response 200 OK (tất cả tasks của mọi user)
```

---

## 5. Chi Tiết API Endpoints

### 5.1. Authentication APIs

| Method | Endpoint | Mô tả | Auth Required |
|--------|----------|-------|--------------|
| POST | `/api/auth/register` | Đăng ký user mới | ❌ |
| POST | `/api/auth/login` | Đăng nhập, nhận JWT token | ❌ |

### 5.2. Task APIs

| Method | Endpoint | Mô tả | Auth Required | Role |
|--------|----------|-------|---------------|------|
| POST | `/api/tasks` | Tạo task mới | ✅ | USER, ADMIN |
| GET | `/api/tasks` | Lấy danh sách tasks (có pagination, sort, filter) | ✅ | USER (chỉ của mình), ADMIN (tất cả) |
| GET | `/api/tasks/{id}` | Lấy task theo ID | ✅ | USER (chỉ của mình), ADMIN (tất cả) |
| PUT | `/api/tasks/{id}` | Cập nhật task | ✅ | USER (chỉ của mình), ADMIN (tất cả) |
| DELETE | `/api/tasks/{id}` | Xóa task | ✅ | USER (chỉ của mình), ADMIN (tất cả) |

**Query Parameters cho GET /api/tasks:**
- `page`: Số trang (mặc định: 0)
- `size`: Số lượng mỗi trang (mặc định: 20)
- `sort`: Sắp xếp (ví dụ: `createdAt,desc` hoặc `deadline,asc`)
- `status`: Lọc theo trạng thái (`PENDING`, `IN_PROGRESS`, `COMPLETED`)

### 5.3. Admin APIs

| Method | Endpoint | Mô tả | Auth Required | Role |
|--------|----------|-------|---------------|------|
| GET | `/api/admin/tasks` | Lấy tất cả tasks của mọi user | ✅ | ADMIN |
| GET | `/api/admin/users` | Lấy danh sách tất cả users | ✅ | ADMIN |

### 5.4. Test API

| Method | Endpoint | Mô tả | Auth Required |
|--------|----------|-------|--------------|
| GET | `/api/hello` | Test endpoint | ❌ |

---

## 6. Cấu Trúc Response

### 6.1. Response Format

Tất cả API đều trả về format chuẩn:

```json
{
  "code": 200,
  "message": "Thông báo",
  "data": { ... }  // hoặc [ ... ] hoặc null
}
```

### 6.2. Error Response

```json
{
  "code": 400,
  "message": "Lỗi validation: {field: error_message}",
  "data": null
}
```

### 6.3. HTTP Status Codes

| Code | Mô tả |
|------|-------|
| 200 | OK - Thành công |
| 201 | Created - Tạo mới thành công |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Chưa đăng nhập hoặc token không hợp lệ |
| 403 | Forbidden - Không có quyền truy cập |
| 404 | Not Found - Không tìm thấy resource |
| 409 | Conflict - Dữ liệu trùng lặp (email đã tồn tại) |
| 500 | Internal Server Error - Lỗi server |

---

## 7. Security

### 7.1. JWT Authentication Flow

```
1. User đăng nhập → Nhận JWT token
2. Client gửi request với header: Authorization: Bearer <token>
3. JwtAuthFilter:
   - Extract token từ header
   - Validate token (signature, expiration)
   - Parse email và userId từ token
   - Load user từ database để lấy role
   - Set Authentication vào SecurityContext
4. Controller/Service sử dụng SecurityContext để lấy current user
```

### 7.2. Authorization Rules

| Endpoint | USER | ADMIN |
|----------|------|-------|
| `/api/auth/**` | ✅ | ✅ |
| `/api/tasks` (GET) | Chỉ tasks của mình | Tất cả tasks |
| `/api/tasks/{id}` | Chỉ task của mình | Tất cả tasks |
| `/api/tasks` (POST) | ✅ Tạo task cho mình | ✅ Tạo task cho mình |
| `/api/tasks/{id}` (PUT/DELETE) | Chỉ task của mình | Tất cả tasks |
| `/api/admin/**` | ❌ | ✅ |

### 7.3. Password Security

- Password được mã hóa bằng **BCrypt** với strength = 10
- Password không bao giờ trả về trong response
- Password được validate: 6-100 ký tự

---

## 8. Tính Năng Chính

### 8.1. Authentication & Authorization
- ✅ Đăng ký user mới
- ✅ Đăng nhập với JWT token
- ✅ Phân quyền USER/ADMIN
- ✅ Bảo vệ API với JWT Bearer token

### 8.2. Task Management
- ✅ CRUD đầy đủ cho Task
- ✅ Phân trang (Pagination)
- ✅ Sắp xếp (Sorting) theo createdAt, deadline
- ✅ Lọc theo status (Filtering)
- ✅ USER chỉ quản lý task của mình
- ✅ ADMIN xem tất cả tasks

### 8.3. Validation & Error Handling
- ✅ Validation input với `@Valid` và Jakarta Validation
- ✅ Global Exception Handler
- ✅ Response format chuẩn
- ✅ Error messages rõ ràng

### 8.4. Documentation
- ✅ Swagger UI để test API
- ✅ OpenAPI documentation
- ✅ JWT authentication trong Swagger

---

## 9. Sơ Đồ Luồng Tổng Quan

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ HTTP Request
       ↓
┌─────────────────────────────────────┐
│      Spring Security Filter Chain    │
│  ┌──────────────────────────────┐   │
│  │   JwtAuthFilter              │   │
│  │   - Extract JWT token        │   │
│  │   - Validate & Set Auth      │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────┐
│         Controller Layer             │
│  - AuthController                   │
│  - TaskController                   │
│  - AdminController                  │
│  - GlobalExceptionHandler           │
└─────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────┐
│          Service Layer               │
│  - UserAuthService                  │
│  - TaskService                      │
│  - UserService                       │
└─────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────┐
│        Repository Layer              │
│  - UserRepository                   │
│  - TaskRepository                    │
└─────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────┐
│         MySQL Database               │
│  - users table                       │
│  - tasks table                       │
└─────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────┐
│      ApiResponse<T>                  │
│  {                                   │
│    "code": 200,                      │
│    "message": "...",                 │
│    "data": { ... }                   │
│  }                                   │
└─────────────────────────────────────┘
       │
       ↓
┌─────────────┐
│   Client    │
└─────────────┘
```

---

## 10. Tóm Tắt

### 10.1. Điểm Mạnh
- ✅ Kiến trúc rõ ràng, dễ bảo trì
- ✅ Security tốt với JWT và BCrypt
- ✅ Phân quyền rõ ràng USER/ADMIN
- ✅ API RESTful chuẩn
- ✅ Validation và error handling đầy đủ
- ✅ Swagger UI để test và document

### 10.2. Công Nghệ Sử Dụng
- **Backend**: Spring Boot 3.5.6 (Java 17)
- **Database**: MySQL 8.4
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **Documentation**: Swagger/OpenAPI 2.8.13

---

**Tài liệu này mô tả đầy đủ về thiết kế, mô hình dữ liệu và luồng API của hệ thống Task Management.**

