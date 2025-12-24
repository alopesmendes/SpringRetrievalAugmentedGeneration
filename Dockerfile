# =============================================================================
# Dockerfile for Image RAG Spring Boot Application
# =============================================================================
# Multi-stage build for optimized production image
# Compatible with Render, Docker, and other container platforms
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy module build files
COPY domain/build.gradle.kts domain/
COPY application/build.gradle.kts application/
COPY infrastructure/build.gradle.kts infrastructure/

# Download dependencies (cached layer)
RUN chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon || true

# Copy source code
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src

# Copy config files
COPY config config

# Build the application
RUN ./gradlew :infrastructure:bootJar -x test --no-daemon

# Extract layers for optimized startup
RUN java -Djarmode=tools -jar infrastructure/build/libs/*.jar extract --layers --launcher --destination extracted

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy extracted layers (optimized for Docker layer caching)
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Render injects PORT environment variable (default: 10000)
ENV PORT=10000
EXPOSE ${PORT}

# Health check for Render
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

# JVM optimization for containers (Render free tier has limited memory)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false"

# Run the application using Spring Boot's layered jar launcher
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dserver.port=${PORT} org.springframework.boot.loader.launch.JarLauncher"]
