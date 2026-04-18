# Stage 1: Build using a pure Ubuntu-based Java image
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Install standard Maven directly from Ubuntu repositories
RUN apt-get update && apt-get install -y maven

# Copy your pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application using the installed Maven
RUN mvn -B -DskipTests clean package

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]