# ===================================================================
# Multi-stage Dockerfile for SPAS Spring Boot Backend
# ===================================================================

# Stage 1: Build the application artifact
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Cache maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production jar (unit tests run in CI/CD pipeline)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal hardened runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthcheck probes
RUN apk add --no-cache curl tzdata

# Create dedicated unprivileged user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled jar from builder
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser:appgroup

# Service port
EXPOSE 8080

# Container health probe checking Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Production JVM options for container environments
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
