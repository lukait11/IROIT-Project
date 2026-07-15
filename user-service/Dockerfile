# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Fetch the OpenTelemetry Java agent
FROM curlimages/curl:8.11.0 AS otel
RUN curl -L -o /tmp/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Stage 3: Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=otel /tmp/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

ENV OTEL_SERVICE_NAME=user-service
ENV OTEL_TRACES_EXPORTER=otlp
# Metrics are already handled by Micrometer + Prometheus (/actuator/prometheus),
# and there's no logs backend, so don't let the agent also try to export those.
ENV OTEL_METRICS_EXPORTER=none
ENV OTEL_LOGS_EXPORTER=none

EXPOSE 8081
ENTRYPOINT [ "java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar" ]
