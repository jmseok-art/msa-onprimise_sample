FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./settings.gradle ./
COPY ./api-gateway/build.gradle api-gateway/
COPY ./api-gateway/src api-gateway/src
RUN ./gradlew :api-gateway:bootJar --no-daemon

RUN chmod +x ./gradlew

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/api-gateway/build/libs/api-gateway.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]