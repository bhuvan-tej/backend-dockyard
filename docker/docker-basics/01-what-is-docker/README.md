# What Is Docker & Why It Exists

## 🎯 Goal
Understand the problem Docker solves, install Docker, and run your first
container — on **macOS or Windows**.

---

## 🧠 The Problem Docker Solves

```
WITHOUT DOCKER
──────────────
  You:    "Works on my machine!"
  Server: "Crashes on mine."

  Root causes:
    Your laptop: Java 17 + macOS
    Server:      Java 11 + Linux
    Colleague:   Java 21 + Windows

  Every environment is slightly different.
  Setup docs go stale. Onboarding takes days.
  "Works on my machine" is a team-wide problem.

WITH DOCKER
───────────
  Your app + JDK version + dependencies + config = one portable container image

  Same image runs on:
    • Your macOS laptop
    • Your colleague's Windows PC
    • The Linux CI server
    • The production Kubernetes cluster

  One command to start. Zero environment differences.
```

---

## 🔑 The 4 Core Terms

```
┌──────────────┬──────────────────────────────────┬─────────────────────┐
│ TERM         │ WHAT IT IS                       │ JAVA ANALOGY        │
├──────────────┼──────────────────────────────────┼─────────────────────┤
│ Image        │ Read-only blueprint / template   │ A Class definition  │
│ Container    │ A running instance of an image   │ new MyClass()       │
│ Dockerfile   │ Instructions to build an image   │ Build script        │
│ Registry     │ Remote storage for images        │ Maven Central       │
└──────────────┴──────────────────────────────────┴─────────────────────┘
```

---

## 🖥️ How Docker Runs on Your Machine

```
macOS
│
├── Docker Desktop  (the app you install — has a GUI)
│       └── A lightweight Linux VM (managed for you)
│               └── Docker Engine  ← containers run here (Linux)
│
└── Terminal (zsh) ──► docker commands ──► Linux VM ──► runs container

Windows
│
├── Docker Desktop  (the app you install — has a GUI)
│       └── WSL2  (Windows Subsystem for Linux 2)
│               └── Docker Engine  ← containers run here (Linux)
│
└── PowerShell ──► docker commands ──► WSL2 ──► runs container

Key point: containers are ALWAYS Linux processes — on macOS and on Windows.
Your OS just routes the command to a hidden Linux engine. It's transparent.

Why Linux? Because 99% of production servers are Linux.
Your local setup matches production exactly — that's the whole point.
```

---

## ✅ Step 1 — Install Docker

### 🍎 macOS
1. Go to https://www.docker.com/products/docker-desktop
2. Download **Docker Desktop for Mac** — pick **Apple Silicon** (M1/M2/M3/M4)
   or **Intel** to match your chip.
3. Open the `.dmg`, drag Docker to Applications, launch it.
4. Wait for the whale icon in the menu bar to stop animating.

### 🪟 Windows
```powershell
# Open PowerShell as Administrator, then:
wsl --install
# Installs WSL2 + Ubuntu. RESTART your PC when prompted.
wsl --status          # after restart — should show: Default Version: 2
```
Then download **Docker Desktop for Windows**, run the installer, tick
**Use WSL 2 backend**, and launch it.

---

## ✅ Step 2 — Verify

```bash
docker --version    # Expected: Docker version 27.x.x or later
docker info         # Confirms the engine is running (errors if Docker Desktop is off)
```
*(Same two commands on macOS and Windows.)*

---

## ✅ Step 3 — Pull and run your first container

```bash
docker run -d -p 8080:80 --name my-first-container nginx

#   -d           run in background (detached)
#   -p 8080:80   map YOUR port 8080 → container's port 80  (MINE:THEIRS)
#   --name       a human-readable name
#   nginx        the image (auto-downloaded from Docker Hub)
```
*(Identical on both platforms.)*

---

## ✅ Step 4 — See it working

```bash
docker ps
# CONTAINER ID  IMAGE  ...  PORTS                  NAMES
# a3f8c1d2e4b5  nginx  ...  0.0.0.0:8080->80/tcp  my-first-container
```

Open it in the browser:

### 🍎 macOS
```bash
open "http://localhost:8080"
```
### 🪟 Windows
```powershell
Start-Process "http://localhost:8080"
```
You should see the **"Welcome to nginx!"** page.

---

## ✅ Step 5 — Look inside the container

```bash
docker exec -it my-first-container bash

# You're now INSIDE a Linux container (on macOS OR Windows):
cat /etc/os-release          # Debian Linux — not your host OS!
ls /usr/share/nginx/html     # the HTML served on port 80
hostname                     # container's random hostname
ps aux                       # processes running inside
exit                         # back to your terminal
```
*(Identical on both platforms — inside is always Linux.)*

---

## ✅ Step 6 — Read the logs

```bash
docker logs my-first-container
```

---

## ✅ Step 7 — Stop and remove

```bash
docker stop my-first-container    # graceful stop (SIGTERM, waits 10s)
docker rm my-first-container      # delete the container
docker ps -a                      # confirm it's gone
```

---

## ✅ Step 8 — Run a different Java version without installing it

```bash
docker run --rm eclipse-temurin:21-jdk java -version

# --rm = auto-remove the container when it exits
# Proves: test ANY Java version without touching your local setup.
```

---

## 🗂️ Commands Reference

```bash
docker run          # Create and start a container from an image
docker ps           # List running containers
docker ps -a        # List ALL containers (including stopped)
docker stop NAME    # Gracefully stop a running container
docker rm NAME      # Remove a stopped container
docker rm -f NAME   # Force-remove a running container
docker exec -it     # Run an interactive command inside a container
docker logs NAME    # View the container's stdout/stderr
docker images       # List locally downloaded images
docker pull IMAGE   # Download an image without running it
docker rmi IMAGE    # Delete a local image
```

> 🧠 **No-memorization check:** every command above is one of Model A's 5 verbs —
> BUILD · RUN · LOOK · ENTER · CLEAN. See the folder's top-level `README.md`.

---

## 📝 Interview Questions This Module Covers

**Q: What is Docker and what problem does it solve?**
> Docker packages an app with all its dependencies into a portable container that runs identically anywhere — solving the "works on my machine" problem and eliminating environment inconsistencies between development, staging, and production.

**Q: How does Docker differ from a Virtual Machine?**
> VMs include a complete OS per machine (gigabytes, minutes to boot, high overhead). Docker containers share the host OS kernel (megabytes, seconds to start, near-native performance). VMs offer stronger isolation; containers offer efficiency.

**Q: What is the difference between a Docker Image and a Container?**
> An image is a static, read-only blueprint stored in layers. A container is a live, running instance of an image — like the relationship between a Class (image) and an Object (container) in Java.

**Q: How does Docker run on macOS and Windows?**
> Neither OS runs containers natively. Docker Desktop runs a lightweight Linux environment — a managed VM on macOS, WSL2 on Windows — and the Docker Engine lives there. Your `docker` commands are transparently routed to that Linux engine, so containers are always Linux processes.