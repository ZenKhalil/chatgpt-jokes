# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 as builder

WORKDIR /app

# Copy your project files to the Docker image
COPY src /app/src
COPY pom.xml /app

# Run Maven build
RUN mvn clean package -DskipTests

# Stage 2: Setup the runtime environment
FROM openjdk:20-oracle

WORKDIR /app

# Copy only the built jar file from the build stage
COPY --from=builder /app/target/chatgpt-meal-plans-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
