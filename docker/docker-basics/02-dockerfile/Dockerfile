# ─────────────────────────────────────────────────────────────────────────
# Dockerfile
#
# Read EVERY comment before running docker build.
# Understand each instruction — don't treat this as boilerplate.
#
# Build:  docker build -t hello-docker:day2 .
# Run:    docker run --rm hello-docker:day2
# Run:    docker run --rm -e MY_NAME="Alice" hello-docker:day2
# ─────────────────────────────────────────────────────────────────────────

# ── FROM ───────────────────────────────────────────────────────────────────
# The FIRST instruction in every Dockerfile.
# Sets the base image — what we're starting FROM.
#
# eclipse-temurin  = official OpenJDK builds maintained by Adoptium
#                    Preferred over openjdk: which is deprecated on Docker Hub
# 17               = Java 17 LTS — match what you have locally
# jdk              = full JDK (includes the javac compiler — we need it for RUN javac)
# alpine           = based on Alpine Linux: ~5 MB vs ~80 MB for Debian
#
# Why a specific tag (17-jdk-alpine) and not "latest"?
# "latest" changes without notice. Pin your tag so builds are reproducible.
FROM eclipse-temurin:17-jdk-alpine

# ── LABEL ───────────────────────────────────────────────────────────────────
# Optional key-value metadata. Visible with: docker inspect hello-docker:day2
LABEL maintainer="YOUR_GITHUB_USERNAME"
LABEL day="02"

# ── WORKDIR ─────────────────────────────────────────────────────────────────
# Sets the working directory INSIDE the container image.
# Every subsequent instruction (COPY, RUN, CMD) runs from this directory.
# Created automatically if it doesn't exist.
# Standard convention for Java apps: /app
WORKDIR /app

# ── COPY ────────────────────────────────────────────────────────────────────
# Copies files FROM your Windows machine INTO the container image.
# Format: COPY <source on host>  <destination in container>
#
# The . destination means "current WORKDIR" = /app
# After this: /app/HelloDocker.java exists in the image
COPY HelloDocker.java .

# ── RUN ─────────────────────────────────────────────────────────────────────
# Executes a command during IMAGE BUILD (not at container start time).
# Each RUN creates a new layer in the image.
# Common uses: install packages, compile code, set permissions.
#
# Here: compile the Java source file. Creates HelloDocker.class in /app
RUN javac HelloDocker.java

# ── ENV ─────────────────────────────────────────────────────────────────────
# Sets environment variables inside the container.
# These are DEFAULT values — can be overridden at runtime.
#
# In Java: read with System.getenv("MY_NAME")
# Override at docker run: docker run -e MY_NAME="Alice" hello-docker:day2
# In Spring Boot: these map directly to application properties
ENV MY_NAME="Docker Learner"
ENV APP_ENV="development"

# ── EXPOSE ──────────────────────────────────────────────────────────────────
# Documents which port the application listens on.
# Does NOT actually open the port (that's -p in docker run).
# Think of it as documentation / a hint to operators.
EXPOSE 8080

# ── CMD ─────────────────────────────────────────────────────────────────────
# The command to execute when the container STARTS.
# This is what makes the container "do something."
#
# ALWAYS use JSON array (exec form):
#   ✅ CMD ["java", "HelloDocker"]   — Java gets SIGTERM directly → graceful shutdown
#   ❌ CMD java HelloDocker          — runs via /bin/sh → shell absorbs SIGTERM
#
# Shell form matters in production: docker stop (and K8s pod termination)
# send SIGTERM. If your app never receives it, it can't shut down gracefully.
CMD ["java", "HelloDocker"]

# ─────────────────────────────────────────────────────────────────────────────
# LAYER CACHE DIAGRAM — why this order matters:
#
#  Step 1: FROM eclipse-temurin:17-jdk-alpine    ← almost never changes = CACHED ✅
#  Step 2: LABEL ...                             ← rarely changes       = CACHED ✅
#  Step 3: WORKDIR /app                          ← never changes        = CACHED ✅
#  Step 4: COPY HelloDocker.java .               ← changes when you edit code ← INVALIDATES cache below
#  Step 5: RUN javac HelloDocker.java            ← must rebuild when code changes
#  Step 6: ENV ...                               ← rebuilds
#  Step 7: CMD ...                               ← rebuilds
#
# If COPY was at Step 1, every change to your code would force ALL steps to rebuild.
# By putting COPY late, steps 1-3 stay cached regardless of code changes.
# ─────────────────────────────────────────────────────────────────────────────
