# Multi-Stage Builds

## 🎯 Goal

---
Understand why single stage builds are a problem in production,
how multi-stage builds solve it, and why this is the standard
approach for all Java and Spring Boot Docker images.

## 🧩 The Problem With Single-Stage Builds

---
```
Single-stage Dockerfile for a Spring Boot app:

  FROM eclipse-temurin:17-jdk       ← 400 MB (compiler + debugger + tools)
  COPY pom.xml .
  RUN mvn clean package             ← Maven also lands in the image
  COPY target/app.jar app.jar
  CMD ["java", "-jar", "app.jar"]

Final image: ~650 MB

What does your running app actually NEED at runtime?
  ✅  JRE (Java Runtime Environment) — to execute the JAR
  ❌  JDK compiler (javac) — only needed to compile, not to run
  ❌  Maven build tool — only needed to build, not to run
  ❌  Source .java files — compiled already, why ship them?
  ❌  Unit test classes — never needed in production

You're shipping the entire kitchen to deliver the meal.
```

## ✅ Multi-Stage Builds: The Solution

---
```
STAGE 1 (builder)              STAGE 2 (runtime)
─────────────────              ──────────────────────
FROM eclipse-temurin:17-jdk    FROM eclipse-temurin:17-jre-alpine
│                               │
│  (full JDK + Maven)           │  (JRE only, ~180 MB)
│  compile & package            │
│                               │
└── COPY --from=builder ────────► app.jar
    (only the JAR crosses)

Stage 1 is THROWN AWAY after build.
Final image = JRE + your JAR only = ~180 MB.
70% smaller than the single-stage version.
```

## Files in This Folder

---
```
01-multi-stage/
├── HelloMultiStage.java      simple Java program used in both builds
├── Dockerfile.single         single stage — the wrong way, for comparison
├── Dockerfile.multistage     multi stage — the right way for production
└── README.md                 you are here
```

## Step 1 — Build Both Images and Compare Size
 
---
```powershell
# Make sure you are in the right folder
cd docker\docker-intermediate\01-multi-stage
 
# Build the single stage image
# -f tells Docker which Dockerfile to use since we have two
# -t app:single tags it with the name app and version single
docker build -f Dockerfile.single -t app:single .
 
# Build the multi stage image
# Same command but points to the multi stage Dockerfile
docker build -f Dockerfile.multistage -t app:multi .
 
# List both images and compare their sizes
# Look at the SIZE column on the right
docker images app
```

## Step 2 — Run Both and Compare Output

---
```powershell
# Run the single stage image
# --rm removes the container automatically when it exits
# APP_NAME is set to Single Stage App in Dockerfile.single
docker run --rm app:single
 
# Run the multi stage image
# APP_NAME is set to Multi Stage App in Dockerfile.multistage
docker run --rm app:multi
 
# Both print the same output but come from very different sized images
```

## Step 3 — Prove the Source Code Is Not in the Multi Stage Image
 
---
```powershell
# Open a shell inside the single stage image
# -it means interactive terminal so the shell stays open
# --rm removes the container when you exit
docker run --rm -it app:single sh
 
# Inside the single stage container — source file IS here
# This is a security risk in production
ls /app
# You will see: HelloMultiStage.java  HelloMultiStage.class
 
# Leave the container
exit
 
# Now open a shell inside the multi stage image
docker run --rm -it app:multi sh
 
# Inside the multi stage container — source file is NOT here
ls /app
# You will see: HelloMultiStage.class only
# The .java source file never made it into this image
 
# Leave the container
exit
```

## Step 4 — Prove the Compiler Is Not in the Multi Stage Image
 
---
```powershell
# Try to run javac inside the multi stage image
# javac is the Java compiler — part of JDK but not JRE
docker run --rm app:multi javac -version
# Error: javac not found
# The compiler does not exist in a JRE only image
# This is exactly what we want for production
 
# Compare with single stage — compiler is still there
docker run --rm app:single javac -version
# Works — but this means the compiler is in your production image
# which is unnecessary and adds attack surface
```

## Step 5 — Clean Up
 
---
```powershell
# Remove both images from your machine
docker rmi app:single
docker rmi app:multi
```

## The Key Syntax to Remember
 
---
```dockerfile
# Give a stage a name with AS
FROM eclipse-temurin:17-jdk-alpine AS builder
 
# Copy from a previous stage using --from=stagename
COPY --from=builder /build/HelloMultiStage.class .
```

## How This Applies to Spring Boot
 
---
In real Spring Boot projects the pattern is the same but uses Maven:
```dockerfile
# Stage 1 — build the JAR file
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml .
# Download all dependencies first as a separate layer
# This layer is cached as long as pom.xml does not change
# so dependency downloads only happen when you add new dependencies
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests
 
# Stage 2 — run the JAR file
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy only the JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
```

You will build exactly this in the Spring Docker section.

## 📝 Interview Questions This Day Covers
 
---
**Q: What is a multi-stage Docker build?**
> A Dockerfile with multiple `FROM` statements where each `FROM` starts a new build stage. Artifacts from earlier stages can be copied into later stages. Only the final stage becomes the output image — all others are discarded.

**Q: Why is multi-stage important for Java/Spring Boot apps?**
> The JDK image is 400 MB+. At runtime you only need the JRE (~180 MB) to execute the JAR. Multi-stage lets you build with the full JDK but ship only the JRE + JAR — reducing image size by ~70%.

**Q: What are the security benefits of multi-stage builds?**
> Source code never ends up in the production image (intellectual property + reduced attack surface). Build tools (Maven, javac) are excluded — fewer installed packages means fewer CVEs.

**Q: What does COPY --from do?**
> Copies a file from a previous build stage into the current stage. The syntax is `COPY --from=STAGE_NAME /path/in/stage /path/in/current`.