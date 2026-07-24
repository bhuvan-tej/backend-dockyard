# Volumes & Networking

## 🎯 Goal
qMaster data persistence (volumes) and container-to-container communication
(networks) — on **macOS or Windows**. These two concepts are the foundation of
every multi-container app.

---

## 💾 Part 1: Volumes — The Data Problem

```
WHAT HAPPENS BY DEFAULT:

  docker run --name app my-image     ← container writes logs.txt inside
  docker stop app
  docker rm app                      ← container deleted
  docker run --name app my-image     ← new container — logs.txt is GONE

  Containers are stateless by design.
  Filesystem inside a container is ephemeral (temporary).
  This is a feature — containers are meant to be disposable.

THE SOLUTION:

  Mount a Volume — storage that lives OUTSIDE the container.
  Container deleted? Volume remains.
  New container mounts the same volume? Data is still there.
```

### Three Volume Types

```
Named Volume       docker run -v my-vol:/app/data myimage
  Docker manages where it's stored (inside the Linux VM / WSL2).
  Best for: databases, persistent app data, production.

Bind Mount         docker run -v /host/folder:/app/data myimage
  You specify the exact host folder (macOS or Windows path).
  Best for: development — edit files on the host, container sees changes live.

tmpfs              docker run --tmpfs /app/tmp myimage
  In-memory only. Gone when container stops.
  Best for: sensitive temporary data (tokens, caches).
```

> 🧠 **Model B:** `-v` = "keep the data". Volume = the container's long-term memory.

---

## 🌐 Part 2: Networking — How Containers Find Each Other

```
THE PROBLEM WITH IPs:

  Container A (app) needs to reach Container B (database).
  Today: container B's IP is 172.17.0.3
  After a restart: container B's IP might be 172.17.0.4
  Your hardcoded URL breaks on every restart.

THE SOLUTION — Docker Network DNS:

  Create a Docker network.
  Both containers join it.
  Docker assigns each container a DNS name = its container name.
  App connects to "postgres" (the name), not 172.17.0.3.
  The name never changes, even if the IP does.
```

> 🧠 Think of a Docker network as a **phone book**: look up a container by *name*,
> not by its ever-changing IP number.

---

## ✅ Exercises

### Exercise 1 — Named Volume: Data Outlives the Container

`docker volume` commands are identical on every OS:

### 🍎 macOS
```bash
docker volume create my-data

# Container 1: write data to the volume
docker run --rm \
  -v my-data:/data \
  alpine sh -c "echo 'Saved by a named volume' > /data/note.txt && cat /data/note.txt"
# Container 1 is GONE (--rm). But the volume persists.

# Container 2: same volume, different container — data still there!
docker run --rm -v my-data:/data alpine cat /data/note.txt

docker volume inspect my-data   # where Docker stores it (inside the Linux VM)
docker volume ls                # list all volumes
docker volume rm my-data        # clean up
```
### 🪟 Windows
```powershell
docker volume create my-data

docker run --rm `
  -v my-data:/data `
  alpine sh -c "echo 'Saved by a named volume' > /data/note.txt && cat /data/note.txt"

docker run --rm -v my-data:/data alpine cat /data/note.txt

docker volume inspect my-data   # where Docker stores it (inside WSL2)
docker volume ls
docker volume rm my-data
```

### Exercise 2 — Bind Mount: Edit on the Host, See It in the Container

### 🍎 macOS
```bash
# Create a folder and file on your Mac
mkdir -p ~/docker-bindtest
echo "Hello from macOS!" > ~/docker-bindtest/note.txt

# Mount your host folder into a container and read the file
docker run --rm -v ~/docker-bindtest:/data alpine cat /data/note.txt

# Edit the file on your Mac, then read it again from a container
nano ~/docker-bindtest/note.txt          # or: open -e ~/docker-bindtest/note.txt
docker run --rm -v ~/docker-bindtest:/data alpine cat /data/note.txt
# It sees the change immediately!

rm -rf ~/docker-bindtest                 # clean up
```
### 🪟 Windows
```powershell
# Create a folder and file on Windows
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\docker-bindtest"
"Hello from Windows!" | Set-Content "$env:USERPROFILE\docker-bindtest\note.txt"

docker run --rm -v "$env:USERPROFILE\docker-bindtest:/data" alpine cat /data/note.txt

notepad "$env:USERPROFILE\docker-bindtest\note.txt"
docker run --rm -v "$env:USERPROFILE\docker-bindtest:/data" alpine cat /data/note.txt

Remove-Item -Recurse "$env:USERPROFILE\docker-bindtest"
```
> This is how Spring Boot hot-reload dev setups work: edit code on the host →
> container picks it up live, no rebuild.

### Exercise 3 — Docker Network: Containers Talking by Name

### 🍎 macOS
```bash
docker network create my-net

# Start a server container (nginx as a simple HTTP server)
docker run -d --name web-server --network my-net nginx

# Start a client on the SAME network — reach the server by its NAME, not IP
docker run --rm --network my-net curlimages/curl curl -s http://web-server/
# "web-server" resolves to nginx's IP automatically — built-in Docker DNS.

# Prove it FAILS without the network (no DNS resolution):
docker run --rm curlimages/curl curl -s --max-time 3 http://web-server/ 2>&1

docker stop web-server && docker rm web-server
docker network rm my-net
```
### 🪟 Windows
```powershell
docker network create my-net

docker run -d --name web-server --network my-net nginx

docker run --rm --network my-net curlimages/curl curl -s http://web-server/

docker run --rm curlimages/curl curl -s --max-time 3 http://web-server/ 2>&1

docker stop web-server && docker rm web-server
docker network rm my-net
```

### Exercise 4 — The Real App+DB Pattern (Preview of Spring + Docker)

### 🍎 macOS
```bash
docker network create app-net

# Start PostgreSQL — its container NAME becomes its DNS hostname
docker run -d \
  --name postgres \
  --network app-net \
  -e POSTGRES_USER=appuser \
  -e POSTGRES_PASSWORD=apppass \
  -e POSTGRES_DB=appdb \
  -v pg-data:/var/lib/postgresql/data \
  postgres:15-alpine

sleep 5   # give it a moment to start

# Connect from ANOTHER container using the name "postgres"
docker run --rm --network app-net postgres:15-alpine \
  psql -h postgres -U appuser -d appdb -c "\l"
# -h postgres  = hostname is the container name!

docker stop postgres && docker rm postgres
docker volume rm pg-data
docker network rm app-net
```
### 🪟 Windows
```powershell
docker network create app-net

docker run -d `
  --name postgres `
  --network app-net `
  -e POSTGRES_USER=appuser `
  -e POSTGRES_PASSWORD=apppass `
  -e POSTGRES_DB=appdb `
  -v pg-data:/var/lib/postgresql/data `
  postgres:15-alpine

Start-Sleep -Seconds 5

docker run --rm --network app-net postgres:15-alpine `
  psql -h postgres -U appuser -d appdb -c "\l"

docker stop postgres && docker rm postgres
docker volume rm pg-data
docker network rm app-net
```

> In Spring Boot `application.yml`:
> `spring.datasource.url=jdbc:postgresql://postgres:5432/appdb`
> The `postgres` in the URL is the **container name = hostname**.

---

## 🗂️ Host Volume Path Reference

```
                     🍎 macOS / Linux                🪟 Windows (PowerShell)
Absolute path        -v /Users/me/data:/app/data     -v C:\Users\Me\data:/app/data
Home folder          -v ~/data:/app/data             -v "$env:USERPROFILE\data:/app/data"
Current directory    -v "$PWD/data:/app/data"        -v "${PWD}\data:/app/data"
Named volume         -v my-volume:/app/data          -v my-volume:/app/data   (same!)

KEY RULE: the CONTAINER side is ALWAYS a Linux path with forward slashes:
  ✅  /app/data
  ❌  \app\data
  ❌  C:\app\data     (inside the container there's no C: drive!)
  ❌  ~/app/data      (no host home inside the container either!)
```

---

## 📝 Interview Questions This Module Covers

**Q: What happens to data when a Docker container is removed?**
> All data written inside the container's own filesystem is lost. To persist data, mount a volume — the volume exists outside the container lifecycle and survives removal.

**Q: What is the difference between a Named Volume and a Bind Mount?**
> Named volume: Docker manages the storage location (ideal for production data like databases). Bind mount: you specify the exact host path (ideal for development where you want live code editing).

**Q: How do containers communicate with each other?**
> Place them on the same Docker network. Docker provides built-in DNS — each container is reachable by its name. The app connects to `postgres` (the container name) rather than a changing IP address.

**Q: Why shouldn't you hardcode container IP addresses?**
> Container IPs are reassigned on every restart and can differ between environments. Docker's built-in DNS (using container names) is stable and environment-independent.

---