# syntax=docker/dockerfile:1

# ============================================================================
#  Stage 1 — BUILD
#  A full JDK + Maven image compiles the app and produces the runnable jar.
#  This stage is heavy but never ships to production.
# ============================================================================
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy the Maven wrapper first so dependency resolution is cached independently
# of source changes. Editing a .java file will NOT re-download dependencies.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy the source and build the jar (skip tests — CI runs them separately).
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ============================================================================
#  Stage 2 — RUNTIME
#  A slim JRE image runs the jar. No Maven, no source, no build tools — just
#  what is needed to run, giving a smaller and more secure final image.
# ============================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Run as a non-root user — a container best practice.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy only the built jar from the build stage.
COPY --from=build /app/target/otp-auth-*.jar app.jar

EXPOSE 8080

# Actuator health endpoint powers Docker/K8s health checks.
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]