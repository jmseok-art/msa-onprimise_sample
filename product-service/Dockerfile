FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY ./gradlew ./
COPY ./gradle ./gradle
COPY ./settings.gradle ./
COPY ./build.gradle ./build.gradle
COPY ./product-service/ product-service/
COPY ./common/ common/
COPY ./gradle.properties ./

RUN chmod +x ./gradlew

RUN ./gradlew :product-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/product-service/build/libs/product-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]