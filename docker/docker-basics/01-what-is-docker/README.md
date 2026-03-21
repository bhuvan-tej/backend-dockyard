# What Is Docker & Why It Exists

## 🎯 Goal
Understand the problem Docker solves, install Docker Desktop on Windows,
and run your first container.

---

## 🧠 The Problem Docker Solves

```
WITHOUT DOCKER
──────────────
  You:    "Works on my machine!"
  Server: "Crashes on mine.".
  
  Root causes:
    Your laptop: Java 17 + Windows
    Server:      Java 11 + Linux
    Colleague:   Java 21 + macOS

  Every environment is slightly different.
  Setup docs go stale. Onboarding takes days.
  "Works on my machine" is a team-wide problem.
 
WITH DOCKER
───────────
  Your app + JDK version + dependencies + config
  = one portable container image

  Same image runs on:
    • Your Windows laptop
    • Your colleague's Mac
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

## 🪟 How Docker Runs on Windows — Understand This First

```
Your Windows PC
│
├── Docker Desktop  (the app you install — has a GUI)
│       │
│       └── WSL2  (Windows Subsystem for Linux 2)
│               │
│               └── Lightweight Linux kernel, hidden inside Windows
│                       │
│                       └── Docker Engine  ← containers run here (Linux)
│
└── PowerShell  ───► docker commands ───► WSL2 ───► runs container

Key point: your containers are Linux processes, even on Windows.
You type in PowerShell → Docker Desktop routes it to WSL2 → containers run.
This is completely transparent. You won't notice it.

Why Linux? Because 99% of production servers are Linux.
Your local setup matches production exactly — that's the whole point.
```

---

## ✅ Step-by-Step: Install & First Container

### Step 1 — Enable WSL2
```powershell
# Open PowerShell as Administrator (right-click → Run as administrator)
wsl --install

# This installs WSL2 + Ubuntu. RESTART your PC when prompted.
# After restart: an Ubuntu terminal may open, set a username/password for it.
# (You won't use Ubuntu directly — Docker Desktop uses it internally.)

# After restart, verify:
wsl --status
# Should show:  Default Version: 2
```

---

### Step 2 — Install Docker Desktop

1. Go to https://www.docker.com/products/docker-desktop
2. Download for Windows (AMD64)
3. Run the installer
4. During install tick  Use WSL 2 backend
5. Start Docker Desktop from Start Menu
6. Wait for the whale in the system tray to stop animating

---

### Step 3 — Verify in PowerShell
```powershell
# Open a NEW PowerShell window after Docker Desktop is running
docker --version
# Expected: Docker version 27.x.x or later

docker info
# Shows Docker system info — confirms the daemon is running
# If this errors, Docker Desktop isn't started yet
```

---

### Step 4 — Pull and run your first container
```powershell
docker run -d -p 8080:80 --name my-first-container nginx

# What each flag does:
#   -d           run in background (detached mode)
#   -p 8080:80   map YOUR port 8080 → container's port 80
#   --name       give it a human-readable name
#   nginx        the image name (auto-downloaded from Docker Hub)
```

---

### Step 5 — See it working
```powershell
# Check it's running
docker ps
# Expected output (something like):
# CONTAINER ID  IMAGE  COMMAND  ...  PORTS                   NAMES
# a3f8c1d2e4b5  nginx  ...      ...  0.0.0.0:8080->80/tcp   my-first-container

# Open in browser
Start-Process "http://localhost:8080"
# You should see: "Welcome to nginx!" page
```

---

### Step 6 — Look inside the container
```powershell
# Open a shell INSIDE the container
# (Even though you're on Windows, this is a Linux shell inside the container)
docker exec -it my-first-container bash

# You are now INSIDE the container. Try:
cat /etc/os-release          # This is Debian Linux — not your Windows!
ls /usr/share/nginx/html     # The HTML file served on port 80
hostname                     # Container's hostname (a random ID)
ps aux                       # Processes running inside
exit                         # Leave the container — back to PowerShell
```

---

### Step 7 — Read the logs
```powershell
docker logs my-first-container
# See nginx access logs from when you visited localhost:8080
```

---

### Step 8 — Stop and remove
```powershell
docker stop my-first-container    # Graceful stop (sends SIGTERM, waits 10s)
docker rm my-first-container      # Delete the container
docker ps -a                      # Confirm it's gone
```

---

### Step 9 — Run a different Java version in Docker
```powershell
# Your machine has Java 17. Let's run Java 21 without installing it:
docker run --rm eclipse-temurin:21-jdk java -version

# --rm = auto-remove the container when it exits
# This proves: you can test any Java version without changing your local setup.
# Pull rate limits may apply — if slow, just move on.
```

---

## 🗂️ Commands Reference

```powershell
docker run          # Create and start a container from an image
docker ps           # List running containers
docker ps -a        # List ALL containers (including stopped ones)
docker stop NAME    # Gracefully stop a running container
docker rm NAME      # Remove a stopped container
docker rm -f NAME   # Force-remove a running container
docker exec -it     # Run an interactive command inside a container
docker logs NAME    # View the container's stdout/stderr output
docker images       # List locally downloaded images
docker pull IMAGE   # Download an image without running it
docker rmi IMAGE    # Delete a local image
```

---

## 📝 Interview Questions This Day Covers

**Q: What is Docker and what problem does it solve?**
> Docker packages an app with all its dependencies into a portable container that runs identically anywhere — solving the "works on my machine" problem and eliminating environment inconsistencies between development, staging, and production.

**Q: How does Docker differ from a Virtual Machine?**
> VMs include a complete OS per machine (gigabytes, minutes to boot, high overhead). Docker containers share the host OS kernel (megabytes, seconds to start, near-native performance). VMs offer stronger isolation; containers offer efficiency.

**Q: What is the difference between a Docker Image and a Container?**
> An image is a static, read-only blueprint stored in layers. A container is a live, running instance of an image — like the relationship between a Class (image) and an Object (container) in Java.

**Q: How does Docker work on Windows specifically?**
> Docker Desktop uses WSL2 (Windows Subsystem for Linux 2) to run a lightweight Linux kernel inside Windows. Containers run as Linux processes inside WSL2. Commands typed in PowerShell are transparently routed to the WSL2 Docker Engine.

---