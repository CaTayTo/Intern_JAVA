# Task Management System

Hệ thống quản lý task với Spring Boot 3, MySQL, JWT Authentication.

## Yêu cầu

- Java 17+
- MySQL 8.0+

## Chạy ứng dụng
### 0. Ứng dụng đã deploy lên render: https://intern-java-yyrf.onrender.com
### 1. Tạo database

```bash
mysql -u root -pRoot@2025 < src/main/resources/data_schema.sql
```

### 2. Chạy ứng dụng

**IntelliJ IDEA:**
- Mở `src/main/java/com/example/demo/AppApplication.java`
- Click Run

**Hoặc Maven:**
```bash
./mvnw.cmd spring-boot:run
```

### 3. Kiểm tra

- API Test: http://localhost:8080/api/hello
- Swagger UI: http://localhost:8080/swagger-ui.html

## Sử dụng

1. Mở Swagger UI: http://localhost:8080/swagger-ui.html
2. Đăng ký user: `POST /api/auth/register`
3. Đăng nhập: `POST /api/auth/login` → Copy token
4. Click "Authorize" → Paste token
5. Test các API khác

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT
- MySQL 8.4
- Swagger/OpenAPI
