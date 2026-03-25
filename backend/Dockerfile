# Multi-stage build for Spring Boot backend
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src src

# Build the application
RUN mvn clean package -B -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Copy the built JAR from builder (using wildcard to match any version)
COPY --from=builder /app/target/ndl-*.jar app.jar

# Wait for postgres, then run
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]

EXPOSE 8080
