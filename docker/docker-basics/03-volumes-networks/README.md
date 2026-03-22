# Volumes & Networking

## 🎯 Goal
Master data persistence (volumes) and container-to-container communication(networks).
These two concepts are the foundation of every multi-container app.

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
  Docker manages where it's stored (inside WSL2 on Windows).
  Best for: databases, persistent app data, production.

Bind Mount         docker run -v C:\my\folder:/app/data myimage
  You specify the exact Windows folder.
  Best for: development — edit files on Windows, container sees changes live.

tmpfs              docker run --tmpfs /app/tmp myimage
  In-memory only. Gone when container stops.
  Best for: sensitive temporary data (tokens, caches).
```

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

---

## ✅ Exercises

### Exercise 1 — Named Volume: Data Outlives the Container

```powershell
# Create a named volume
docker volume create my-data

# Container 1: write data to the volume
docker run --rm `
  -v my-data:/data `
  alpine sh -c "echo 'Saved by a named volume' > /data/note.txt && cat /data/note.txt"
# Container 1 is GONE (--rm). But the volume persists.

# Container 2: same volume, different container — data still there!
docker run --rm `
  -v my-data:/data `
  alpine cat /data/note.txt

# Inspect where Docker stores this on Windows (inside WSL2)
docker volume inspect my-data
# "Mountpoint" is inside WSL2 — Docker manages it, you don't need to touch it

# List all volumes
docker volume ls

# Clean up
docker volume rm my-data
```

### Exercise 2 — Bind Mount: Edit on Windows, See It in the Container

```powershell
# Create a folder and file on your Windows machine
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\docker-bindtest"
"Hello from Windows!" | Set-Content "$env:USERPROFILE\docker-bindtest\note.txt"

# Mount your Windows folder into a container and read the file
docker run --rm `
  -v "$env:USERPROFILE\docker-bindtest:/data" `
  alpine cat /data/note.txt

# Edit the file on Windows (open in Notepad, change the text, save)
notepad "$env:USERPROFILE\docker-bindtest\note.txt"

# Read it again from the container — sees the change immediately!
docker run --rm `
  -v "$env:USERPROFILE\docker-bindtest:/data" `
  alpine cat /data/note.txt

# This is how Spring Boot hot-reload dev setups work.
# Edit code on Windows → container picks it up live, no rebuild.

# Clean up
Remove-Item -Recurse "$env:USERPROFILE\docker-bindtest"
```

### Exercise 3 — Docker Network: Containers Talking by Name

```powershell
# Create a custom network
docker network create my-net

# Start a server container (nginx as a simple HTTP server)
docker run -d --name web-server --network my-net nginx

# Start a client container on the SAME network
# Access the server using its NAME — not its IP!
docker run --rm `
  --network my-net `
  curlimages/curl `
  curl -s http://web-server/
# "web-server" in the URL resolves to nginx's container IP automatically.
# This is Docker DNS — built-in, zero configuration.

# Prove it FAILS without the network (no DNS resolution):
docker run --rm `
  curlimages/curl `
  curl -s --max-time 3 http://web-server/ 2>&1
# Connection refused / timeout — different network, can't find it

# Clean up
docker stop web-server && docker rm web-server
docker network rm my-net
```

### Exercise 4 — The Real App+DB Pattern (Preview of Week 3)

```powershell
# This is the exact pattern used in Spring Boot + Docker!
docker network create app-net

# Start PostgreSQL — its container NAME becomes its DNS hostname
docker run -d `
  --name postgres `
  --network app-net `
  -e POSTGRES_USER=appuser `
  -e POSTGRES_PASSWORD=apppass `
  -e POSTGRES_DB=appdb `
  -v pg-data:/var/lib/postgresql/data `
  postgres:15-alpine

# Give it 5 seconds to start
Start-Sleep -Seconds 5

# Connect from ANOTHER container using the name "postgres"
docker run --rm `
  --network app-net `
  postgres:15-alpine `
  psql -h postgres -U appuser -d appdb -c "\l"
# -h postgres  = hostname is the container name!

# In Spring Boot application.yml (Week 3):
#   spring.datasource.url=jdbc:postgresql://postgres:5432/appdb
#                                           ^^^^^^^^
#                                           Container name = hostname

# Clean up
docker stop postgres && docker rm postgres
docker volume rm pg-data
docker network rm app-net
```

---

## 🪟 Windows Volume Path Reference

```powershell
# Absolute Windows path:
docker run -v C:\Users\YourName\data:/app/data myimage

# Environment variable for user home:
docker run -v "$env:USERPROFILE\data:/app/data" myimage

# Current directory (PowerShell):
docker run -v "${PWD}\data:/app/data" myimage

# Named volumes (no Windows path needed):
docker run -v my-volume:/app/data myimage

# KEY RULE: Container path always uses forward slashes (Linux-style):
#   ✅  /app/data
#   ❌  \app\data
#   ❌  C:\app\data    (inside the container there's no C drive!)
```

---

## 📝 Interview Questions This Day Covers

**Q: What happens to data when a Docker container is removed?**
> All data written inside the container's own filesystem is lost. To persist data, mount a volume — the volume exists outside the container lifecycle and survives removal.

**Q: What is the difference between a Named Volume and a Bind Mount?**
> Named volume: Docker manages the storage location (ideal for production data like databases). Bind mount: you specify the exact host path (ideal for development where you want live code editing).

**Q: How do containers communicate with each other?**
> Place them on the same Docker network. Docker provides built-in DNS — each container is reachable by its name. The app connects to `postgres` (the container name) rather than a changing IP address.

**Q: Why shouldn't you hardcode container IP addresses?**
> Container IPs are reassigned on every restart and can differ between environments. Docker's built-in DNS (using container names) is stable and environment-independent.

---