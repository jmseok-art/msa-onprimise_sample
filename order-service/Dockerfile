FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./settings.gradle ./
COPY ./order-service/build.gradle order-service/
COPY ./order-service/src order-service/src

RUN chmod +x ./gradlew

RUN ./gradlew :order-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/order-service/build/libs/order-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]