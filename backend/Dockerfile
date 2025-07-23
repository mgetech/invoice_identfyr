FROM gradle:8.5.0-jdk21 AS build

WORKDIR /app

COPY build.gradle settings.gradle /app/

# Copy the gradle wrapper
COPY gradlew /app/
COPY gradle /app/gradle

# Make gradlew executable
RUN chmod +x ./gradlew

COPY src /app/src

RUN ./gradlew build -x test --no-daemon

# Stage 2: Create the final image with source code for testing
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY gradlew /app/
COPY gradle /app/gradle

# Make gradlew executable
RUN chmod +x ./gradlew

COPY build.gradle /app/
COPY settings.gradle /app/

# Copy the source code (this is crucial for running tests)
COPY src /app/src

# Copy the built jar file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]