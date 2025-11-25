# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml và source code
COPY pom.xml .
COPY src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy JAR từ builder (artifactId đã đổi thành task_management)
COPY --from=builder /app/target/task_management-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
