# ===== build stage =====
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# gradle wrapper 반드시 포함
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/

RUN chmod +x /app/gradlew

# 소스 복사 후 빌드
COPY src /app/src
RUN ./gradlew --no-daemon clean bootJar

# ===== runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
