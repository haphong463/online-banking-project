# Sử dụng OpenJDK 17 làm base image
FROM openjdk:17-jdk-alpine

# Thư mục làm việc trong container
WORKDIR /app

# Copy file jar vào container
COPY target/online-banking-1.0.0.jar app.jar

# Expose cổng mà ứng dụng sẽ chạy
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java","-jar","app.jar"]
