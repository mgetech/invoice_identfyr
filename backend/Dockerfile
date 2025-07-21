# Stage 1: Build the application
FROM gradle:8.5.0-jdk21-alpine AS build

WORKDIR /app

# Copy the build.gradle and settings.gradle files
COPY build.gradle settings.gradle /app/

# Copy the gradle wrapper
COPY gradlew /app/
COPY gradle /app/gradle

# Copy the source code
COPY src /app/src

# Build the application (but don't run tests yet to avoid circular dependency)
RUN ./gradlew build -x test --no-daemon

# Stage 2: Create the final image with source code for testing
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the gradlew and gradle directory
COPY gradlew /app/
COPY gradle /app/gradle

# Copy Gradle build files
COPY build.gradle /app/
COPY settings.gradle /app/

# Copy the source code (this is crucial for running tests)
COPY src /app/src

# Copy the built jar file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]