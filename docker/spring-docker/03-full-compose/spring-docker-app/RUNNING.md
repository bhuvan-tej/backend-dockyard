# 🚀 Running Guide — Full Production Stack

---

## 🖥️ Running Locally in IntelliJ

### 🔌 Step 1 — Start Dependencies Only
```powershell
# Navigate to the app folder
cd docker\spring-docker\03-full-compose\spring-docker-app

# Start only postgres and redis in Docker
# This lets IntelliJ run the Spring Boot app
# while the databases run in containers
docker compose up -d postgres redis
```

### ⚙️ Step 2 — Configure Run Configuration in IntelliJ
```
1. Click Run → Edit Configurations
2. Select SpringDockerAppApplication
3. Click Modify options → Add VM options
4. Add: -Dspring.profiles.active=local
5. Click Apply → OK
```

### ▶️ Step 3 — Run the App
```
Open SpringDockerAppApplication.java in IntelliJ
Click the green play button next to the main method
Wait for this line in the console:
  Started SpringDockerAppApplication in X seconds
```

### 🔥 Step 4 — Enable Hot Reload
```
Spring Boot DevTools is already in pom.xml
When you change a Java file IntelliJ recompiles automatically
DevTools restarts the app — no manual stop and start needed

To enable in IntelliJ:
  File → Settings → Build, Execution, Deployment
    → Compiler → tick Build project automatically

  File → Settings → Advanced Settings
    → tick Allow auto-make to start even if developed application is running
```

### 🛑 Step 5 — Stop When Done
```powershell
# Stop the databases
docker compose down
```

---

## 🐳 Running in Docker

### ▶️ Step 1 — Stop Previous Stack
```powershell
# Stop 02-redis-cache if it is still running
# You cannot have two stacks using the same ports at the same time
cd docker\spring-docker\02-redis-cache\spring-docker-app
docker compose down
```

### 🏗️ Step 2 — Build and Start
```powershell
# Navigate to 03-full-compose
cd docker\spring-docker\03-full-compose\spring-docker-app

# Build the app image and start all four services
# --build forces Docker to rebuild the Spring Boot image
# This is needed whenever you change Java code or the Dockerfile
docker compose up -d --build
```

### 📋 Step 3 — Watch Startup Order
```powershell
# Follow logs from all services
# Watch the four services start in this order:
#   1. postgres starts — runs pg_isready healthcheck every 5s
#   2. redis starts    — runs redis-cli ping healthcheck every 5s
#   3. app starts      — waits for postgres and redis to be healthy
#                        runs Actuator healthcheck every 10s
#   4. nginx starts    — waits for app to be healthy
#                        only then starts accepting traffic on port 80
docker compose logs -f
```

Press Ctrl+C to stop following. Containers keep running.

### ✅ Step 4 — Verify All Services Are Up
```powershell
# Show status of all four services
# All should show running or healthy
docker compose ps
```

### 🛑 Step 5 — Stop When Done
```powershell
# Stop all containers and remove them
# Volumes are kept — your database data is safe
docker compose down

# Stop and also delete all volumes
# Use this only when you want a completely fresh start
# Warning: this permanently deletes all database data
docker compose down -v
```

---

## 🔁 Rebuilding After Code Changes

```powershell
# After changing any Java file rebuild the image
# --build forces a fresh image build
# -d keeps it running in the background
docker compose up -d --build

# Watch the app restart
docker compose logs -f app
```

---

## 📋 Useful Commands While Running

```powershell
# See all running containers and their status
docker compose ps

# Follow logs from all services at once
docker compose logs -f

# Follow logs from one service only
docker compose logs -f app
docker compose logs -f nginx
docker compose logs -f postgres
docker compose logs -f redis

# Open a shell inside a running service
docker compose exec app sh
docker compose exec nginx sh
docker compose exec postgres psql -U appuser -d appdb
docker compose exec redis redis-cli

# Restart one service without touching others
docker compose restart app
```