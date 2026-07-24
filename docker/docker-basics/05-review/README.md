# Review

## 🎯 Goal
Review every concept. Fill any gaps. Run the final summary exercise —
on **macOS or Windows**.

---

## ✅ Self-Check

Work through each item. If you can do it **from the 3 mental models** (not rote
memory), you understand it. If you can't, revisit that module's README.

**Docker Basics**
- [ ] Run `docker run -d -p 8080:80 --name test nginx` and see it in the browser
- [ ] List running containers: `docker ps`
- [ ] Shell inside a running container: `docker exec -it test bash`
- [ ] Stop and remove: `docker stop test && docker rm test`

**Dockerfile**
- [ ] Write a Dockerfile with FROM, WORKDIR, COPY, RUN, ENV, CMD from scratch
- [ ] Build an image: `docker build -t myapp:v1 .`
- [ ] Override ENV at runtime: `docker run -e KEY=val myapp:v1`
- [ ] Explain why layer order matters for caching (Model C)

**Volumes & Networking**
- [ ] Create a named volume, write to it, remove the container, prove data persists
- [ ] Use a bind mount to share a host folder with a container
- [ ] Create a Docker network and have two containers talk by name

**Debugging**
- [ ] Read logs from a stopped container
- [ ] Copy a file FROM a container to your Desktop
- [ ] Check a container's exit code with `docker inspect`
- [ ] Explain what exit code 137 means

---

## 🔄 Concepts Map

```
CONCEPT           WHAT IT IS                               KEY COMMAND
──────────────    ──────────────────────────────────       ────────────────────────────
Image             Blueprint (read-only layers)             docker build / docker pull
Container         Running instance of an image             docker run
Dockerfile        Instructions to build an image           (text file you author)
Layer             One Dockerfile instruction = 1 layer     docker history <image>
Volume            Persistent storage outside container     docker volume create
Bind Mount        Host folder → container path             -v /host/path:/container/path
Network           Virtual LAN for containers               docker network create
DNS               Containers find each other by name       (automatic on custom networks)
Registry          Remote storage for images                docker push / docker pull
```

---

## 🧪 Final Exercise: Data-Persistent Java App

Build a Java app that:
1. Reads `YOUR_NAME` from an environment variable
2. Appends a timestamped greeting to `/app/logs/app.log`
3. Runs in Docker with a named volume for the logs
4. Proves the log persists across container restarts

### Step 1 — Create FinalExercise.java

Create this file inside the `05-review` folder:

```java
import java.io.*;
import java.time.LocalDateTime;

public class FinalExercise {
    public static void main(String[] args) throws Exception {

        // Read the YOUR_NAME environment variable; fall back to "Developer"
        String name = System.getenv("YOUR_NAME");
        if (name == null) name = "Developer";

        // Create the logs directory inside the container if it does not exist
        new File("/app/logs").mkdirs();

        // Build the log message with the current timestamp
        String message = "[" + LocalDateTime.now() + "] Hello " + name + " — Basics complete!";

        // Print to console so docker logs can show it
        System.out.println(message);

        // Append the message to the log file (true = append, not overwrite)
        try (PrintWriter pw = new PrintWriter(new FileWriter("/app/logs/app.log", true))) {
            pw.println(message);
        }

        System.out.println("Log written to /app/logs/app.log");
    }
}
```

### Step 2 — Create a Dockerfile

Create this file inside the `05-review` folder:

```dockerfile
# Start from the official Java 17 JDK on Alpine Linux (~5MB base)
FROM eclipse-temurin:17-jdk-alpine

# Set /app as the working directory inside the container
WORKDIR /app

# Copy the source from your machine into /app inside the image
COPY FinalExercise.java .

# Compile at build time → FinalExercise.class ends up in /app
RUN javac FinalExercise.java

# Default value for YOUR_NAME; override with docker run -e YOUR_NAME="Alice"
ENV YOUR_NAME="Basics Graduate"

# Document that this container writes logs to /app/logs
VOLUME /app/logs

# Run the compiled Java class when the container starts
CMD ["java", "FinalExercise"]
```

### Step 3 — Build the image

### 🍎 macOS
```bash
cd docker/docker-basics/05-review
docker build -t basics-final .
```
### 🪟 Windows
```powershell
cd docker\docker-basics\05-review
docker build -t basics-final .
```

### Step 4 — First run (with a named volume attached)

### 🍎 macOS
```bash
docker run --rm \
  -e YOUR_NAME="YourActualName" \
  -v basics-logs:/app/logs \
  basics-final
```
### 🪟 Windows
```powershell
docker run --rm `
  -e YOUR_NAME="YourActualName" `
  -v basics-logs:/app/logs `
  basics-final
```

### Step 5 — Second run (new container, same volume)

### 🍎 macOS
```bash
docker run --rm \
  -e YOUR_NAME="YourActualName" \
  -v basics-logs:/app/logs \
  basics-final
```
### 🪟 Windows
```powershell
docker run --rm `
  -e YOUR_NAME="YourActualName" `
  -v basics-logs:/app/logs `
  basics-final
```

### Step 6 — Read the accumulated log file

```bash
# Spin up a throwaway Alpine container just to read the shared volume
docker run --rm -v basics-logs:/logs alpine cat /logs/app.log
# You should see TWO entries — one per run.
# This proves the log survived across container removals.
```
*(Identical on both platforms.)*

### Step 7 — Clean up

```bash
docker volume rm basics-logs   # deletes the log file permanently
```

---

> 🎓 **You've finished Docker Basics.** You can now build images, run containers,
> persist data, connect containers, and debug failures — on macOS or Windows,
> all from the 3 mental models in the folder's top-level `README.md`.