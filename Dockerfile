FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x ./gradlew

COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S spring && adduser -S -G spring spring

WORKDIR /home/spring
COPY --from=builder /app/build/libs/*.jar /home/spring/deploy.jar
USER spring

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/home/spring/deploy.jar"]
