FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p uploads

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8183
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
