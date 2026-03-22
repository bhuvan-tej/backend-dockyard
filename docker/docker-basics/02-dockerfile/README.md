# Writing Your First Dockerfile

## 🎯 Goal
Write a Dockerfile, understand every instruction, build an image,
run it, and observe Docker's layer caching in action.

---

## 🏗️ Dockerfile → Image → Container

```
Dockerfile               Image                    Container
(you write this)    →    (Docker builds this)  →  (Docker runs this)
Text instructions        Layered package          Live process

Each Dockerfile instruction = one cached layer.
Unchanged layers reuse cache → builds go from 3 min → 10 seconds.
```

---

## 🔑 Dockerfile Instructions

```
FROM        → Start from this base image           (always first)
WORKDIR     → Set working directory inside image
COPY        → Copy file from your PC into image
RUN         → Execute command at BUILD time        (installs, compiles)
ENV         → Set environment variable
EXPOSE      → Document which port the app listens on
CMD         → Default command when container STARTS (overridable)
ENTRYPOINT  → Fixed executable; CMD provides its arguments
```

---

## 📁 Files

```
02-dockerfile\
├── README.md          ← you are here
├── HelloDocker.java   ← Java program to containerise
└──  Dockerfile         ← heavily commented — read every line
```

---

## ✅ Step-by-Step

### Step 1 — Compile the Java file manually first
```powershell
cd docker-basics\02-dockerfile

# Compile and run it locally so you know what it should do
javac HelloDocker.java
java HelloDocker
# See the output. Now Docker will do exactly this — but inside a container.
```

### Step 2 — Read the Dockerfile
Open `Dockerfile`. Read every line including the comments.
Don't build yet — just understand what each instruction does.

### Step 3 — Build the image
```powershell
docker build -t hello-docker:day2 .

# -t  = tag the image with name:version
# .   = build context (send this folder to the Docker daemon)

# Watch the output. Each "Step X/Y" is one Dockerfile instruction = one layer.
```

### Step 4 — Inspect the image
```powershell
# See your new image
docker images hello-docker

# See each layer and its size (notice tiny layers for each instruction)
docker history hello-docker:day2

# Full metadata in JSON
docker inspect hello-docker:day2
```

### Step 5 — Run it
```powershell
# Basic run (auto-remove when done)
docker run --rm hello-docker:day2

# Override the ENV variable without rebuilding the image!
docker run --rm -e MY_NAME="Alice" hello-docker:day2
docker run --rm -e MY_NAME="Bob" -e APP_ENV="staging" hello-docker:day2
```

### Step 6 — Run in background and explore inside
```powershell
# Start in background so we can exec into it
docker run -d --name day2 hello-docker:day2

# Shell into the running container
docker exec -it day2 sh

# You're now INSIDE the Linux container — explore:
ls /app                     # see HelloDocker.java and HelloDocker.class
env                         # see all env variables including MY_NAME
cat /etc/os-release         # Alpine Linux (even though you're on Windows)
java -version               # Java version from the image, not your machine
exit

# Cleanup
docker stop day2 && docker rm day2
```

### Step 7 — See caching in action (most important exercise!)
```powershell
# Run the build again without changing anything
docker build -t hello-docker:day2 .
# ALL steps say "CACHED" — rebuild took < 1 second

# Now add a print line to HelloDocker.java in Notepad / VS Code
# Then rebuild:
docker build -t hello-docker:day2 .
# Notice: only the COPY step and below rebuild. Steps above = CACHED.
# This is WHY layer order matters — stable things first, changing things last.
```

---

## ⚠️ Important Windows Note: exec form vs shell form

```dockerfile
# CMD as shell string (avoid this):
CMD java HelloDocker
# Runs as: /bin/sh -c "java HelloDocker"
# Problem: when you do docker stop, the SHELL catches SIGTERM — Java never sees it.
# Result: app can't shut down gracefully.

# CMD as JSON array (always use this):
CMD ["java", "HelloDocker"]
# Runs Java directly — Java receives SIGTERM and can shut down cleanly.
# This matters in production when K8s stops your container.
```

---

## 📝 Interview Questions This Day Covers

**Q: What does each Dockerfile instruction do?**
> `FROM` sets the base image. `WORKDIR` sets the working directory. `COPY` copies files from host to image. `RUN` executes a command at build time. `ENV` sets environment variables. `EXPOSE` documents the port. `CMD` sets the default startup command.

**Q: What is the difference between RUN and CMD?**
> `RUN` executes during the image **build** phase (installs software, compiles code). `CMD` executes when the container **starts**. You can override `CMD` at `docker run` time; you cannot override `RUN` at runtime.

**Q: Why does layer order in a Dockerfile matter?**
> Docker caches each layer. If a layer changes, every layer **below** it must rebuild from scratch — no cache. Put rarely-changing layers (base image, dependency downloads) at the top. Put frequently-changing layers (your source code) at the bottom.

**Q: What is .dockerignore?**
> Like `.gitignore` for Docker. Lists files to exclude from the build context. Always exclude `target\`, `.git\`, `.idea\` — reduces build time and prevents secrets accidentally entering images.

---