FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./settings.gradle ./
COPY ./product-service/build.gradle product-service/
COPY ./product-service/src product-service/src

RUN ./gradlew :product-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/product-service/build/libs/product-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]