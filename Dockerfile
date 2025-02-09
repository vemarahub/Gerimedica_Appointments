# Use official Java 17 image (or your Java version)
FROM openjdk:17-jdk-slim

# Set work directory
WORKDIR /app

# Copy built JAR file into container
COPY target/*.jar app.jar

# Expose application port
EXPOSE 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
