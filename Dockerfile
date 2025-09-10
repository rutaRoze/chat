# Build stage
FROM gradle:8.7.0-jdk17 AS build
WORKDIR /home/gradle/project
COPY . .
RUN ./gradlew build --no-daemon

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/chat-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
