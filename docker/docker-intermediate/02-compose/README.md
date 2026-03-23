# Docker Compose

## 🎯 Goal

---
Understand what Docker Compose is, why it exists, and how to use it
to run a full multi-container application with a single command.
By the end you will have a Java app talking to PostgreSQL,
both running in containers, wired together automatically.

## 🤔 Why Docker Compose Exist

---
```
Without Compose you would have to run all of this manually every time:
 
  docker network create app-network
 
  docker run -d \
    --name postgres \
    --network app-network \
    -e POSTGRES_USER=appuser \
    -e POSTGRES_PASSWORD=apppass \
    -e POSTGRES_DB=appdb \
    -v postgres_data:/var/lib/postgresql/data \
    postgres:15-alpine
 
  docker run -d \
    --name app \
    --network app-network \
    -p 8080:8080 \
    -e DB_URL=jdbc:postgresql://postgres:5432/appdb \
    -e DB_USER=appuser \
    -e DB_PASS=apppass \
    my-app:latest
 
  That is 2 long commands just to start.
  Stopping, restarting and cleaning up is even more tedious.
 
With Compose all of that becomes:
 
  docker compose up -d
 
One command starts everything — builds images, creates network, creates volumes, starts containers in the right order.
```

## 🔑 Compose YAML Structure

---
```
services:              # Each key = one container
  postgres:
    image: ...         # Which image to use
    environment: {}    # ENV variables
    ports: []          # Host:container port mapping
    volumes: []        # Mounts
    healthcheck: {}    # How to test if it's ready
    depends_on: {}     # Start ordering
    networks: []       # Which network(s) to join
    restart: ...       # Restart policy

volumes: {}            # Named volume definitions
networks: {}           # Custom network definitions

 Docker Compose reads this file and:
  1. Creates the network
  2. Creates the volumes
  3. Builds any images that have a build section
  4. Starts containers in dependency order, postgres starts first because app depends_on postgres
```

## 📁 Files in this Folder

---
```
02-compose/
├── app/
│   ├── Main.java           Java HTTP server that connects to PostgreSQL
│   └── Dockerfile          multi-stage build for the app
├── docker-compose.yml      defines both services, network and volume
├── QUICK_REFERENCE.md      summary of Docker compose commands
└── README.md               you are here
```

## Step 1 — Start the Full Stack

---
```powershell
# Make sure you are in the 02-compose folder
# docker-compose.yml must be in the current folder for these commands to work
cd docker\docker-intermediate\02-compose
 
# Start all services in the background
# -d means detached, runs without blocking your terminal
# Docker Compose will:
#   build the app image from app/Dockerfile
#   pull the postgres:15-alpine image from Docker Hub
#   create the app-network network
#   create the postgres_data volume
#   start postgres first, wait for it to be healthy
#   then start the app
docker compose up -d
```

## Step 2 — Watch the Logs

---
```powershell
# Follow logs from all services at once
# Each line is prefixed with the service name so you know which container it came from
# Press Ctrl+C to stop following — the containers keep running
docker compose logs -f
 
# Follow logs from only the app service
# Useful when postgres is noisy and you only want to see app output
docker compose logs -f app
 
# Follow logs from only the postgres service
docker compose logs -f postgres
```

## Step 3 — See All Running Services

---
```powershell
# Show the status of all services defined in docker-compose.yml
# You should see both postgres and app with status running
docker compose ps
```

## Step 4 — Test the App
 
---
```powershell
# Open the app in your browser
# The app increments a visit counter stored in PostgreSQL on every request
Start-Process "http://localhost:8080"
 
# Or use curl from the terminal
# curl sends an HTTP request and prints the response
curl http://localhost:8080
 
# Refresh the browser or run curl again
# The visits number should increase each time
# The count is stored in PostgreSQL so it persists across app restarts
curl http://localhost:8080
```

## Step 5 — Prove the Database Is Separate From the App
 
---
```powershell
# Restart only the app container without touching postgres
# The visit count should continue from where it left off
# because the data lives in postgres, not in the app container
docker compose restart app
 
# Check the app again — visits continue from before
curl http://localhost:8080
```

## Step 6 — Shell Into a Running Service
 
---
```powershell
# Open a shell inside the postgres container
# docker compose exec works like docker exec but uses the service name
# psql is the PostgreSQL command line client
# -U appuser connects as our database user
# -d appdb connects to our database
docker compose exec postgres psql -U appuser -d appdb
 
# Inside psql you can run SQL commands
# List all tables
\dt
 
# Check the visit count directly in the database
SELECT * FROM visits;
 
# Exit psql
\q
 
# Open a shell inside the app container
docker compose exec app sh
 
# Look around inside the app container
ls /app
env
exit
```

## Step 7 — Scale a Service

---
```powershell
# Run 3 instances of the app service
# Useful for understanding how load balancing would work
# Note: this will fail on port 8080 because you cannot bind the same port twice
# This is why production setups use a load balancer in front
docker compose up -d --scale app=3
 
# See all three app containers running
docker compose ps
 
# Scale back down to 1
docker compose up -d --scale app=1
```

## Step 8 — Stop Everything
 
---
```powershell
# Stop all containers and remove them
# The postgres_data volume is preserved so your data is safe
docker compose down
 
# Start again — data is still there
docker compose up -d
curl http://localhost:8080
# Visit count continues from where it was before stopping
```

## Step 9 — Stop and Wipe Everything
 
---
```powershell
# Stop all containers, remove them, AND delete the volumes
# This wipes the database completely
# Use this when you want a completely fresh start
# WARNING: all data in postgres_data is permanently deleted
docker compose down -v
 
# Start fresh — visit count starts from 1 again
docker compose up -d
curl http://localhost:8080
```

## 📝 Interview Questions

---
**Q: What is Docker Compose and when do you use it?**
> Docker Compose defines a multi-container application in a YAML file
and manages it with single commands. Use it for local development
and simple single-host deployments. For multi-host production
deployments at scale, use Kubernetes.

**Q: What does depends_on with condition service_healthy do?**
> It makes one service wait until another service passes its
healthcheck before starting. Without the condition it only waits
for the container to start, not for the service inside to be ready.
PostgreSQL needs a few seconds after the container starts before
it accepts connections.

**Q: How does service discovery work in Docker Compose?**
> All services in a Compose file share a network. Docker provides
built-in DNS so each service is reachable by its service name.
The app connects to postgres using the hostname postgres which
Docker resolves to the postgres container IP automatically.

**Q: What is the difference between docker compose down and down -v?**
> down removes containers and the network but keeps volumes.
down -v also removes all named volumes which deletes your data.
Never use -v unless you want to start completely fresh.