# Review

## 🎯 Goal
Review every concept. Fill any gaps. Run the final summary exercise.

---

## ✅ Self-Check

Work through each item. If you can do it **from memory**, you know it.
If you can't, revisit that day's README and redo the exercise.

**Docker Basics**
- [ ] Run `docker run -d -p 8080:80 --name test nginx` and see it in the browser
- [ ] List running containers: `docker ps`
- [ ] Shell inside a running container: `docker exec -it test bash`
- [ ] Stop and remove: `docker stop test && docker rm test`

**Dockerfile**
- [ ] Write a Dockerfile with FROM, WORKDIR, COPY, RUN, ENV, CMD from scratch
- [ ] Build an image: `docker build -t myapp:v1 .`
- [ ] Override ENV at runtime: `docker run -e KEY=val myapp:v1`
- [ ] Explain to yourself why layer order matters for caching

**Volumes & Networking**
- [ ] Create a named volume, write to it, remove the container, prove data persists
- [ ] Use a bind mount to share a Windows folder with a container
- [ ] Create a Docker network and have two containers talk by name

**Debugging**
- [ ] Read logs from a stopped container
- [ ] Copy a file FROM a container to your Desktop
- [ ] Check a container's exit code with `docker inspect`
- [ ] Explain what exit code 137 means

---

## 🔄 Week 1 Concepts Map

```
CONCEPT           WHAT IT IS                               KEY COMMAND
──────────────    ──────────────────────────────────       ────────────────────────────
Image             Blueprint (read-only layers)             docker build / docker pull
Container         Running instance of an image             docker run
Dockerfile        Instructions to build an image           (text file you author)
Layer             One Dockerfile instruction = 1 layer     docker history <image>
Volume            Persistent storage outside container     docker volume create
Bind Mount        Windows folder → container path          -v C:\path:/container/path
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
4. Prove the log persists across container restarts

### Step 1 — Create FinalExercise.java

Create this file inside `05-review` folder in IntelliJ:

```java
import java.io.*;
import java.time.LocalDateTime;
 
public class FinalExercise {
    public static void main(String[] args) throws Exception {
 
        // Read the YOUR_NAME environment variable
        // If it is not set fall back to "Developer"
        String name = System.getenv("YOUR_NAME");
        if (name == null) name = "Developer";
 
        // Create the logs directory inside the container if it does not exist
        new File("/app/logs").mkdirs();
 
        // Build the log message with current timestamp
        String message = "[" + LocalDateTime.now() + "] Hello " + name + " — Week 1 complete!";
 
        // Print to console so docker logs can show it
        System.out.println(message);
 
        // Append the message to the log file
        // true means append, not overwrite
        try (PrintWriter pw = new PrintWriter(new FileWriter("/app/logs/app.log", true))) {
            pw.println(message);
        }
 
        System.out.println("Log written to /app/logs/app.log");
    }
}
```

### Step 2 — Create a Dockerfile

Create this file inside `05-review` folder:

```dockerfile
# Start from the official Java 17 JDK on Alpine Linux
# Alpine is used because it is very small, only about 5MB
FROM eclipse-temurin:17-jdk-alpine
 
# Set /app as the working directory inside the container
# Every command below runs from this directory
WORKDIR /app
 
# Copy FinalExercise.java from your Windows machine into /app inside the image
COPY FinalExercise.java .
 
# Compile the Java file at build time
# After this step FinalExercise.class exists in /app
RUN javac FinalExercise.java
 
# Set a default value for YOUR_NAME
# You can override this at runtime with docker run -e YOUR_NAME="Alice"
ENV YOUR_NAME="Week1 Graduate"
 
# Tell Docker this container writes logs to /app/logs
# This is just documentation, it does not create the volume automatically
VOLUME /app/logs
 
# Run the compiled Java class when the container starts
CMD ["java", "FinalExercise"]
```

### Step 3 — Build the image

```powershell
# Navigate to the 05-review folder first
# Make sure you are in the right folder before building
cd docker\docker-basics\05-review
 
# Build the image and tag it as week1-final
# The dot at the end means use the current folder as the build context
# Docker will look for Dockerfile in this folder
docker build -t week1-final .
```

### Step 4 — First run

```powershell
# Run the container with a named volume attached
# --rm means automatically remove the container when it exits
# -e YOUR_NAME="YourActualName" overrides the default ENV variable
# -v week1-logs:/app/logs mounts a named volume called week1-logs
#    to /app/logs inside the container so the log file is saved outside the container
docker run --rm `
  -e YOUR_NAME="YourActualName" `
  -v week1-logs:/app/logs `
  week1-final
```

### Step 5 — Second run with a new container

```powershell
# Run again — this is a brand new container
# but it uses the same named volume week1-logs
# so it can read what the previous container wrote
docker run --rm `
  -e YOUR_NAME="YourActualName" `
  -v week1-logs:/app/logs `
  week1-final
```

### Step 6 — Read the accumulated log file

```powershell
# Spin up a temporary Alpine container just to read the log file
# --rm removes it automatically when done
# -v week1-logs:/logs mounts the same named volume to /logs in this container
# alpine is the image — tiny Linux, good for quick one-off tasks
# cat /logs/app.log prints the file contents
docker run --rm -v week1-logs:/logs alpine cat /logs/app.log
 
# You should see TWO entries — one from each run
# This proves the log survived across container removals
```

### Step 7 — Clean up

```powershell
# Remove the named volume now that the exercise is done
# Warning: this deletes the log file permanently
docker volume rm week1-logs
```
 
---