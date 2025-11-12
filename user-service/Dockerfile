FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./settings.gradle ./
COPY ./user-service/build.gradle user-service/
COPY ./user-service/src user-service/src

RUN chmod +x ./gradlew

RUN ./gradlew :user-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/user-service/build/libs/user-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]