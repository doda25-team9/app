# Use Maven with Java 25
FROM maven:3.9-eclipse-temurin-25

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Rename JAR to fixed name
RUN mv target/*.jar target/app.jar

# Expose port 8080
EXPOSE 8080

# Run the application  
ENTRYPOINT ["java", "-jar", "target/app.jar"]