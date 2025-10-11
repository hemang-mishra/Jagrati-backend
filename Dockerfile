FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar jagrati_app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/jagrati_app.jar"]

