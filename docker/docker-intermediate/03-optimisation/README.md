# Image Optimisation

## 🎯 Goal

---
Learn the techniques that make Docker images smaller, builds faster and production images safer.
These are habits every developer should have from day one — not something you fix later.

## ⚡ The Four Optimisation Techniques

---
```
1. .dockerignore       stop sending junk to the Docker daemon
2. Layer caching       order instructions so cache is reused
3. Smaller base images use alpine variants to reduce image size
4. Multi-stage builds  already covered in 01-multi-stage
```

## Technique 1 — .dockerignore

---
Every time you run docker build, Docker sends the entire build context
folder to the Docker daemon before it starts building.

```
Without .dockerignore                With .dockerignore
 
Your project folder:                 Docker only sees:
  src/          2 MB                   src/          2 MB
  target/       150 MB   ──────►       pom.xml       5 KB
  .git/         50 MB                  Dockerfile    1 KB
  .idea/        30 MB
  node_modules/ 200 MB               Total sent: 2 MB instead of 432 MB
  pom.xml       5 KB
  Dockerfile    1 KB
 
  Total sent: 432 MB
```
The `.dockerignore` file is already in this folder.
Open it and read every comment — each exclusion has a reason.

### See the difference yourself

---
```powershell
# Navigate to the optimisation folder
cd docker\docker-intermediate\03-optimisation
 
# Create a dummy large file to simulate a target folder
# fsutil creates a file of exactly 100MB filled with zeros
# This simulates what Maven puts in target/ after a build
fsutil file createnew big-file.bin 104857600
 
# Check the file was created and is 100MB
# dir shows files with their sizes in bytes
dir big-file.bin
 
# Build an image WITHOUT .dockerignore
# First temporarily rename .dockerignore so Docker ignores it
Rename-Item .dockerignore .dockerignore.bak
 
# Build and watch how long it takes to send the build context
# The first line Docker prints shows how many bytes were sent
# Notice it includes the 100MB big-file.bin
docker build -t opt-test:no-ignore .
 
# Now restore .dockerignore
Rename-Item .dockerignore.bak .dockerignore
 
# Add big-file.bin to .dockerignore temporarily to demonstrate
# Open .dockerignore and add this line: big-file.bin
 
# Build again with .dockerignore active
# The build context is much smaller because big-file.bin is excluded
docker build -t opt-test:with-ignore .
 
# Clean up
Remove-Item big-file.bin
docker rmi opt-test:no-ignore
docker rmi opt-test:with-ignore
```

## Technique 2 — Layer Caching Order

---
Docker caches each layer. When a layer changes all layers below it must rebuild.
Ordering layers correctly means most of them stay cached between builds, and you only rebuild what actually changed.

```
BAD ORDER — cache breaks on every code change:
 
  FROM eclipse-temurin:17-jdk-alpine
  COPY src/ .                     ← your code, changes constantly
  RUN mvn dependency:go-offline   ← downloads 50MB of dependencies
  RUN mvn package                 ← compiles
 
  Every time you change one line of code:
    COPY invalidates the cache
    Maven downloads all dependencies again  (50MB every build)
    Maven compiles everything again
 
GOOD ORDER — dependencies cached separately from code:
 
  FROM eclipse-temurin:17-jdk-alpine
  COPY pom.xml .                  ← only changes when you add a dependency
  RUN mvn dependency:go-offline   ← downloads dependencies ONCE, cached
  COPY src/ .                     ← your code, changes constantly
  RUN mvn package                 ← only recompiles, dependencies already cached
 
  When you change one line of code:
    pom.xml COPY → CACHED (pom.xml did not change)
    dependency download → CACHED (pom.xml did not change)
    src/ COPY → rebuilds (code changed)
    compile → rebuilds
 
  Result: no dependency downloads on code changes
```

### See caching in action

```powershell
# Use the Dockerfile from 02-dockerfile as an example
# Build it twice and watch the second build use cache
cd docker\docker-basics\02-dockerfile
 
# First build — all steps run from scratch
docker build -t cache-test:v1 .
 
# Second build without changes — all steps say CACHED
# This takes less than 1 second
docker build -t cache-test:v1 .
 
# Now change one line in HelloDocker.java
# Just add a space anywhere and save the file
 
# Third build — COPY and below rebuild, FROM and WORKDIR stay cached
docker build -t cache-test:v1 .
 
# Clean up
docker rmi cache-test:v1
cd docker\docker-intermediate\03-optimisation
```

## Technique 3 — Choosing Smaller Base Images

---
The base image you choose in FROM has the biggest impact on image size.
Always use the smallest image that has what you need.

```powershell
# Pull three different Java base images and compare their sizes
# This download takes a few minutes depending on your connection
 
# Full Ubuntu based JDK — includes everything, very large
docker pull eclipse-temurin:17-jdk
 
# Alpine based JDK — same Java, tiny Linux base
docker pull eclipse-temurin:17-jdk-alpine
 
# Alpine based JRE — runtime only, no compiler
docker pull eclipse-temurin:17-jre-alpine
 
# Compare the sizes of all three
# Look at the SIZE column — the difference is significant
docker images eclipse-temurin
 
# Clean up all three images
docker rmi eclipse-temurin:17-jdk
docker rmi eclipse-temurin:17-jdk-alpine
docker rmi eclipse-temurin:17-jre-alpine
```

### Image size comparison

```
eclipse-temurin:17-jdk           ~460 MB   full JDK on Ubuntu
eclipse-temurin:17-jdk-alpine    ~360 MB   full JDK on Alpine
eclipse-temurin:17-jre-alpine    ~200 MB   JRE only on Alpine
 
For production Spring Boot apps:
  Build stage   uses 17-jdk-alpine    to compile with Maven
  Runtime stage uses 17-jre-alpine    to run the JAR
 
Combining multi-stage with alpine gives you roughly 200MB
instead of 460MB — more than half the size
```

## Technique 4 — Combining Everything

---
Here is what an optimised Dockerfile for a Spring Boot app looks like.
You will build exactly this in the spring-docker section.

```dockerfile
# Stage 1 — build
# Use Alpine JDK to keep the builder stage small
FROM eclipse-temurin:17-jdk-alpine AS builder
 
WORKDIR /build
 
# Copy pom.xml first — separate layer from source code
# As long as pom.xml does not change this layer stays cached
# Maven will not re-download dependencies on every code change
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
 
# Download all dependencies into the local Maven cache
# -B means batch mode, no interactive prompts
# This layer is cached until pom.xml changes
RUN ./mvnw dependency:go-offline -B
 
# Now copy source code — this layer changes on every code edit
COPY src ./src
 
# Build the JAR file, skip tests for speed
# -DskipTests skips running tests during build
RUN ./mvnw clean package -DskipTests -B
 
# Stage 2 — runtime
# Use Alpine JRE — smaller, no compiler, no build tools
FROM eclipse-temurin:17-jre-alpine AS runtime
 
WORKDIR /app
 
# Copy only the JAR from the builder stage
# Source code, Maven, compiler — all left behind
COPY --from=builder /build/target/*.jar app.jar
 
# Tell Java to respect container memory limits
# Without this Java reads your host RAM not the container limit
# and sets heap size too large, causing OOMKilled errors
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
 
EXPOSE 8080
 
# Use shell form here so JAVA_OPTS environment variable is expanded
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Optimisation Checklist

---
Before pushing any image to production run through this list:

```
[ ] .dockerignore exists and excludes target/, .idea/, .git/
[ ] pom.xml is copied before src/ so dependency cache is separate
[ ] Multi-stage build used — JDK for build, JRE for runtime
[ ] Alpine variant used as base image
[ ] No secrets or .env files in the image
[ ] JAVA_OPTS includes UseContainerSupport and MaxRAMPercentage
[ ] Source .java files are not in the final image
```

## 📝 Interview Questions

---
**Q: What is .dockerignore and why is it important?**
> It excludes files from the build context sent to the Docker daemon.
Without it Maven target folders, IDE files and git history get
sent on every build wasting time and risking secrets leaking
into images.

**Q: How do you optimise Docker layer caching for a Java project?**
> Copy pom.xml first and run dependency downloads as a separate
RUN step before copying source code. Dependencies rarely change
so that layer stays cached. Source code changes trigger only
the compile step, not another dependency download.

**Q: Why use Alpine base images?**
> Alpine Linux is around 5MB compared to Ubuntu at 80MB.
Using eclipse-temurin:17-jre-alpine instead of eclipse-temurin:17-jdk
reduces the base image from 460MB to 200MB with no loss of
runtime functionality.

**Q: What does UseContainerSupport do in Java?**
> It tells the JVM to read memory limits from the container
rather than the host machine. Without it Java sees your host
RAM of say 32GB and sets an 8GB heap, then gets OOMKilled
because the container limit is only 512MB.