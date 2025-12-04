FROM maven:3.9-eclipse-temurin-25 AS builder

# Accept build arguments from workflow
ARG GITHUB_TOKEN
ARG GITHUB_ACTOR

WORKDIR /app

# Create Maven settings.xml with GitHub authentication
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /root/.m2/settings.xml && \
    echo '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0' >> /root/.m2/settings.xml && \
    echo '  https://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '  <servers>' >> /root/.m2/settings.xml && \
    echo '    <server>' >> /root/.m2/settings.xml && \
    echo '      <id>github</id>' >> /root/.m2/settings.xml && \
    echo "      <username>\${env.GITHUB_ACTOR}</username>" >> /root/.m2/settings.xml && \
    echo "      <password>\${env.GITHUB_TOKEN}</password>" >> /root/.m2/settings.xml && \
    echo '    </server>' >> /root/.m2/settings.xml && \
    echo '  </servers>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml && \
    export GITHUB_TOKEN="${GITHUB_TOKEN}" && \
    export GITHUB_ACTOR="${GITHUB_ACTOR}"

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application with environment variables
RUN --mount=type=cache,target=/root/.m2 \
    GITHUB_TOKEN=${GITHUB_TOKEN} GITHUB_ACTOR=${GITHUB_ACTOR} \
    mvn clean package -DskipTests

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

ENTRYPOINT ["sh", "-c", "java -Dserver.port=$APP_PORT -jar app.jar"]
