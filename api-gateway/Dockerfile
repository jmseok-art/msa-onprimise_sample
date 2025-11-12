FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY ./gradlew ./
COPY ./gradle ./gradle
COPY ./settings.gradle ./
COPY ./api-gateway/ api-gateway/
COPY ./gradle.properties ./

RUN chmod +x ./gradlew
RUN ./gradlew :api-gateway:bootJar --no-daemon


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/api-gateway/build/libs/api-gateway.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]