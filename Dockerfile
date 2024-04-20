# Use an official OpenJDK runtime as a parent image
FROM openjdk:20-oracle

# Set the working directory to /app
WORKDIR /app

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
CMD java -Dserver.port=${PORT:-8080} -jar app.jar
