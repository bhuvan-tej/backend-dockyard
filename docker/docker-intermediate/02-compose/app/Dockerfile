# app/Dockerfile
#
# Multi-stage build for the compose demo app.
# Stage 1 compiles the Java file.
# Stage 2 runs it with only the JRE.
#
# This Dockerfile is not built directly with docker build.
# Docker Compose builds it automatically when you run docker compose up.
# The build section in docker-compose.yml points to this folder.

# ── Stage 1 — compile ────────────────────────────────────────────────

FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy the Java source file into the builder stage
COPY Main.java .

# Download the PostgreSQL JDBC driver so the app can talk to PostgreSQL
# wget fetches the file from Maven Central
# The driver JAR lets Java connect to PostgreSQL databases
RUN wget -q https://jdbc.postgresql.org/download/postgresql-42.7.1.jar

# Compile Main.java with the PostgreSQL driver on the classpath
# -cp postgresql-42.7.1.jar tells javac where to find the JDBC classes
RUN javac -cp postgresql-42.7.1.jar Main.java

# ── Stage 2 — runtime ────────────────────────────────────────────────

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the compiled class file from the builder stage
COPY --from=builder /build/Main.class .

# Copy the PostgreSQL driver JAR — needed at runtime to connect to the database
COPY --from=builder /build/postgresql-42.7.1.jar .

# The app listens on port 8080
# Docker Compose maps this to port 8080 on your Windows machine
EXPOSE 8080

# Run the app with the PostgreSQL driver on the classpath
# -cp . includes the current directory for Main.class
# -cp postgresql-42.7.1.jar includes the driver JAR
CMD ["java", "-cp", ".:postgresql-42.7.1.jar", "Main"]