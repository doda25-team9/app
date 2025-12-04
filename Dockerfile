FROM maven:3.9-eclipse-temurin-25 AS builder

ARG GITHUB_TOKEN
ARG GITHUB_ACTOR

WORKDIR /app

# Create directory AND settings.xml
RUN mkdir -p /root/.m2 && \
    echo "<settings><servers><server><id>github</id><username>${GITHUB_ACTOR}</username><password>${GITHUB_TOKEN}</password></server></servers></settings>" > /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests
RUN mv target/*.jar target/app.jar

FROM eclipse-temurin:25-jre-alpine-3.22 AS final

WORKDIR /app

ENV APP_PORT=8080
ENV MODEL_HOST=http://localhost:8081

EXPOSE 8080

COPY --from=builder /app/target/app.jar .

ENTRYPOINT ["sh", "-c", "java -Dserver.port=$APP_PORT -jar app.jar"]
