# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /usr/src/app
COPY . .
RUN mvn clean package -DskipTests


# Stage 2: Create a minimal runtime image
FROM eclipse-temurin-21-jdk-alpine
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
