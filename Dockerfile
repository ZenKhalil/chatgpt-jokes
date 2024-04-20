# Use an official OpenJDK runtime as a parent image
FROM openjdk:20-oracle

# Set the working directory to /app
WORKDIR /app

# Copy the jar file into the container at /app
COPY target/chatgpt-meal-plans-0.0.1-SNAPSHOT.jar /app/app.jar

# Make port 8080 available to the world outside this container
ENV PORT 8080

# Run the jar file using shell form to allow variable expansion
CMD java -Dserver.port=$PORT -jar app.jar
