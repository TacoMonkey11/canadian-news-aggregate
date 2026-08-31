# Build Stage
FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || true
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Runtime Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY feeds.txt ./feeds.txt

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]