# Dockerfile — spring-docker-app
#
# Multi-stage build for the Spring Boot application.
# Stage 1 builds the JAR using Maven.
# Stage 2 runs the JAR using only the JRE.
#
# Build:  docker build -t spring-docker-app:latest .
# Run:    docker compose up -d  (use docker-compose.yml instead of running directly)

# ─────────────────────────────────────────────────────────────────────
# STAGE 1 — builder
#
# Uses the full JDK and Maven to compile and package the app.
# This entire stage is discarded after the build.
# Only the JAR file crosses into stage 2.
# ─────────────────────────────────────────────────────────────────────

# AS builder gives this stage a name so we can reference it in stage 2
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set the working directory for all commands in this stage
WORKDIR /build

# Copy the Maven wrapper files first
# The Maven wrapper lets us build without Maven installed on the host
# These files change very rarely so this layer stays cached most of the time
COPY mvnw .
COPY .mvn .mvn

# Give the Maven wrapper execute permission
# This is needed because the file comes from Windows and Linux requires
# explicit execute permission on scripts
RUN chmod +x mvnw

# Copy pom.xml before the source code
# This is the dependency caching trick:
#   pom.xml changes only when you add or remove a dependency
#   source code changes on every feature or bug fix
# By copying pom.xml first and downloading dependencies as a separate layer
# Maven only re-downloads dependencies when pom.xml changes
# not on every code change which saves minutes on every rebuild
COPY pom.xml .

# Download all dependencies declared in pom.xml
# -B means batch mode — no interactive prompts, cleaner output
# go-offline means download everything needed to build without internet later
# This layer is CACHED as long as pom.xml does not change
RUN ./mvnw dependency:go-offline -B

# Now copy the source code
# This layer changes on every code edit
# But because dependencies are already downloaded in the layer above
# Maven does not re-download anything — it just compiles
COPY src ./src

# Build the JAR file
# clean removes any previous build output
# package compiles the code and creates the JAR in target/
# -DskipTests skips running tests during the Docker build
# Tests should be run in CI before the Docker build, not inside it
# -B batch mode for cleaner output
RUN ./mvnw clean package -DskipTests -B

# Print the JAR file to confirm it was created successfully
RUN ls -la target/*.jar

# ─────────────────────────────────────────────────────────────────────
# STAGE 2 — runtime
#
# Uses only the JRE — no compiler, no Maven, no build tools.
# Contains only what is needed to run the Spring Boot JAR.
# This is the final image that gets pushed to GHCR.
# ─────────────────────────────────────────────────────────────────────

# Fresh start from a smaller base image
# JRE = Java Runtime Environment — can run JARs but cannot compile Java
# Alpine keeps the image small
FROM eclipse-temurin:17-jre-alpine AS runtime

# Set the working directory inside the runtime container
WORKDIR /app

# Copy ONLY the built JAR from the builder stage
# --from=builder means take this file from the stage named builder
# target/*.jar matches the JAR file Maven created
# app.jar is the name we give it in the runtime image
COPY --from=builder /build/target/*.jar app.jar

# JVM flags for running inside a container
# UseContainerSupport tells the JVM to read memory limits from the container
# instead of the host machine — critical to avoid OOMKilled errors
# Without this the JVM sees your host RAM of say 32GB and sets an 8GB heap
# then gets killed because the container limit is only 512MB
# MaxRAMPercentage=75.0 means use 75% of the container memory for the heap
# The remaining 25% is for non-heap memory like threads and metaspace
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Tell Docker this container listens on port 8080
# This is documentation — the actual port mapping is in docker-compose.yml
EXPOSE 8080

# Start the Spring Boot app
# We use shell form here so the JAVA_OPTS environment variable is expanded
# exec java $JAVA_OPTS -jar app.jar is the actual command that runs
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]