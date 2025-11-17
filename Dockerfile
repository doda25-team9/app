# Use Maven with Java 25
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Rename JAR to fixed name
RUN mv target/*.jar target/app.jar


FROM eclipse-temurin:25-jre-alpine-3.22 AS final

WORKDIR /app

# Set default app and model-service ports
ENV APP_PORT=8080
ENV MODEL_HOST=http://localhost:8081

# Expose port 8080
EXPOSE 8080

COPY --from=builder /app/target/app.jar .

ENTRYPOINT ["sh", "-c", "java -Dserver.port=$APP_PORT -Dmodel.url=$MODEL_HOST -jar app.jar"]
