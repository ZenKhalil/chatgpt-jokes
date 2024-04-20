# Use a Maven base image to handle the build
FROM maven:3.8.4-openjdk-20-slim AS build
WORKDIR /app

# Copy your pom.xml and source code into the image
COPY pom.xml .
COPY src ./src

# Build your application
RUN mvn clean package -DskipTests

# Start with a clean, smaller runtime image
FROM openjdk:20-slim
WORKDIR /app

# Copy the built jar file from the previous stage
COPY --from=build /app/target/chatgpt-meal-plans-0.0.1-SNAPSHOT.jar /app/app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "/app/app.jar"]
