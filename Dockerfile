# --- Stage 1: Build Phase ---
FROM azul/zulu-openjdk:17 AS builder
WORKDIR /app

# Copy gradle wrapper and build files first for optimal layer caching
COPY gradlew settings.gradle build.gradle gradle.lockfile sonar-project.properties ./
COPY gradle ./gradle

# Normalize the wrapper in case it was checked out with Windows line endings.
RUN sed -i 's/\r$//' ./gradlew \
    && chmod +x ./gradlew \
    && ./gradlew dependencies --no-daemon

# Copy source code and build the application jar
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# --- Stage 2: Runtime Phase ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root system user
RUN groupadd -g 1000 appgroup && useradd -u 1000 -g appgroup -s /bin/sh appuser
USER appuser:appgroup

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/project-portal.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]