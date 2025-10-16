# Multi-stage build for jobs-service
FROM gradle:7.6.1-jdk11 AS builder

WORKDIR /app
COPY . .
RUN gradle :jobs-service:build -x test --no-daemon

FROM openjdk:11-jre-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r microjobs && useradd -r -g microjobs microjobs

# Copy the built jar
COPY --from=builder /app/jobs-service/build/libs/*.jar app.jar

# Change ownership
RUN chown microjobs:microjobs app.jar

USER microjobs

EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
