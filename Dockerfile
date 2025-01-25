# Step 1: Use an official Java runtime as the base image
FROM openjdk:21-jdk

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the JAR file into the container
COPY target/ms-user-management-service-0.0.1-SNAPSHOT.jar /app/

# Step 4: Expose the application port (match it with your Spring Boot app's server.port)
EXPOSE 8098

# Step 5: Run the application
ENTRYPOINT ["java", "-jar", "ms-user-management-service-0.0.1-SNAPSHOT.jar"]
