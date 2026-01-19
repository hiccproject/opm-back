# 1. 빌드 스테이지
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
# 권한 부여 및 빌드 (테스트 제외로 속도 향상)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]