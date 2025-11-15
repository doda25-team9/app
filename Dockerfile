# Use Maven with Java 25
FROM maven:3.9-eclipse-temurin-25

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the application  
CMD ["sh", "-c", "java -jar target/*.jar"]