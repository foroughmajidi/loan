# Step 1: Use an official OpenJDK image as the base image
FROM openjdk:17-jdk-slim

# Step 2: Set environment variables for Spring application
ENV SPRING_APPLICATION_NAME=LoanSystem
ENV SERVER_PORT=8080

# Step 3: Set working directory inside the container
WORKDIR /app

# Step 4: Copy the JAR file into the container (adjusted path to reflect correct location)
COPY target/loan-system-0.0.1-SNAPSHOT.jar /app/loan-system.jar

# Step 5: Expose the application port
EXPOSE 8080

# Step 6: Run the application
ENTRYPOINT ["java", "-jar", "/app/loan-system.jar"]
