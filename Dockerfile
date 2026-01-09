FROM eclipse-temurin:25-jre-alpine-3.22

WORKDIR /app

ENV APP_PORT=8080
ENV MODEL_HOST=http://localhost:8081

EXPOSE 8080

# Just copy the pre-built JAR (built outside Docker)
COPY target/app.jar .

ENTRYPOINT ["sh", "-c", "java -Dserver.port=$APP_PORT -jar app.jar"]