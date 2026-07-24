# Writing Your First Dockerfile

## 🎯 Goal
Write a Dockerfile, understand every instruction, build an image,
run it, and observe Docker's layer caching in action — on **macOS or Windows**.

---

## 🏗️ Dockerfile → Image → Container

```
Dockerfile               Image                    Container
(you write this)    →    (Docker builds this)  →  (Docker runs this)
Text instructions        Layered package          Live process

Each Dockerfile instruction = one cached layer.
Unchanged layers reuse cache → builds go from 3 min → 10 seconds.
```

> 🧠 **Model C:** put what changes *least* at the top, what changes *most* at the
> bottom. Then caching just works. (See the folder's top-level `README.md`.)

---

## 🔑 Dockerfile Instructions

```
FROM        → Start from this base image           (always first)
WORKDIR     → Set working directory inside image
COPY        → Copy file from your machine into image
RUN         → Execute command at BUILD time        (installs, compiles)
ENV         → Set environment variable
EXPOSE      → Document which port the app listens on
CMD         → Default command when container STARTS (overridable)
ENTRYPOINT  → Fixed executable; CMD provides its arguments
```

---

## 📁 Files

```
02-dockerfile/
├── README.md          ← you are here
├── HelloDocker.java   ← Java program to containerise
└── Dockerfile         ← heavily commented — read every line
```

---

## ✅ Step-by-Step

### Step 1 — Compile the Java file manually first

### 🍎 macOS
```bash
cd docker/docker-basics/02-dockerfile
javac HelloDocker.java
java HelloDocker
# See the output. Now Docker will do exactly this — but inside a container.
```
### 🪟 Windows
```powershell
cd docker\docker-basics\02-dockerfile
javac HelloDocker.java
java HelloDocker
```

### Step 2 — Read the Dockerfile
Open `Dockerfile`. Read every line including the comments.
Don't build yet — just understand what each instruction does.

### Step 3 — Build the image
```bash
docker build -t hello-docker:v2 .

# -t  = tag the image with name:version
# .   = build context (send this folder to the Docker engine)
# Watch the output — each "Step X/Y" is one Dockerfile instruction = one layer.
```
*(Identical on both platforms.)*

### Step 4 — Inspect the image
```bash
docker images hello-docker             # see your new image
docker history hello-docker:v2         # each layer and its size
docker inspect hello-docker:v2         # full metadata in JSON
```

### Step 5 — Run it
```bash
docker run --rm hello-docker:v2                                   # basic run
docker run --rm -e MY_NAME="Alice" hello-docker:v2               # override ENV
docker run --rm -e MY_NAME="Bob" -e APP_ENV="staging" hello-docker:v2
```

### Step 6 — Run in background and explore inside
```bash
docker run -d --name v2 hello-docker:v2   # start detached
docker exec -it v2 sh                      # shell into it

# You're now INSIDE the Linux container — explore:
ls /app                     # HelloDocker.java and HelloDocker.class
env                         # env variables including MY_NAME
cat /etc/os-release         # Alpine Linux (regardless of your host OS)
java -version               # Java from the image, not your machine
exit

docker stop v2 && docker rm v2   # cleanup
```

### Step 7 — See caching in action (most important exercise!)
```bash
# Rebuild without changing anything
docker build -t hello-docker:v2 .
# ALL steps say "CACHED" — rebuild took < 1 second
```
Now edit `HelloDocker.java` (add a print line) and rebuild:

### 🍎 macOS
```bash
open -e HelloDocker.java      # or: nano HelloDocker.java
docker build -t hello-docker:v2 .
# Only the COPY step and below rebuild. Steps above = CACHED.
```
### 🪟 Windows
```powershell
notepad HelloDocker.java
docker build -t hello-docker:v2 .
```
> This is WHY layer order matters — stable things first, changing things last.

---

## ⚠️ Important: exec form vs shell form (same on every OS)

```dockerfile
# CMD as shell string (avoid this):
CMD java HelloDocker
# Runs as: /bin/sh -c "java HelloDocker"
# Problem: on docker stop, the SHELL catches SIGTERM — Java never sees it.
# Result: app can't shut down gracefully.

# CMD as JSON array (always use this):
CMD ["java", "HelloDocker"]
# Runs Java directly — Java receives SIGTERM and shuts down cleanly.
# This matters in production when K8s stops your container.
```

---

## 📝 Interview Questions This Module Covers

**Q: What does each Dockerfile instruction do?**
> `FROM` sets the base image. `WORKDIR` sets the working directory. `COPY` copies files from host to image. `RUN` executes a command at build time. `ENV` sets environment variables. `EXPOSE` documents the port. `CMD` sets the default startup command.

**Q: What is the difference between RUN and CMD?**
> `RUN` executes during the image **build** phase (installs software, compiles code). `CMD` executes when the container **starts**. You can override `CMD` at `docker run` time; you cannot override `RUN` at runtime.

**Q: Why does layer order in a Dockerfile matter?**
> Docker caches each layer. If a layer changes, every layer **below** it must rebuild from scratch — no cache. Put rarely-changing layers (base image, dependency downloads) at the top. Put frequently-changing layers (your source code) at the bottom.

**Q: What is .dockerignore?**
> Like `.gitignore` for Docker. Lists files to exclude from the build context. Always exclude `target/`, `.git/`, `.idea/` — reduces build time and prevents secrets accidentally entering images.