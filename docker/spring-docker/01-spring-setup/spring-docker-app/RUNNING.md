## 🖥️ Running Locally in IntelliJ

---
### 🔌 Step 1 — Start PostgreSQL in Docker

```powershell
# Start a local PostgreSQL container
# The credentials match what is in application.yml
# -d runs it in the background
# --name gives it a readable name
# -e sets environment variables PostgreSQL uses on first startup
# -p maps your Windows port 5432 to the container port 5432
# postgres:15-alpine is a small official PostgreSQL image
docker run -d `
  --name local-postgres `
  -e POSTGRES_USER=appuser `
  -e POSTGRES_PASSWORD=apppass `
  -e POSTGRES_DB=appdb `
  -p 5432:5432 `
  postgres:15-alpine
```

### ▶️ Step 2 — Run the App

Open `SpringDockerAppApplication.java` in IntelliJ and click the
green play button next to the main method.

Wait for this line in the console:
```
Started SpringDockerAppApplication in X seconds
```

### ✅ Step 3 — Test the API

Use the IntelliJ HTTP client — open `test.http` in the controller folder
and click the green play button next to each request.

Or use curl from the IntelliJ terminal:

```powershell
# Create a product
# -X POST sets the HTTP method to POST
# -H "Content-Type: application/json" tells the server we are sending JSON
# -d sets the request body
curl -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Laptop\",\"description\":\"Gaming laptop\",\"price\":999.99,\"stock\":10}"

# Get all products
# No -X flag needed — GET is the default method
curl http://localhost:8080/api/products

# Get product with id 1
curl http://localhost:8080/api/products/1

# Update product with id 1
curl -X PUT http://localhost:8080/api/products/1 `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Laptop Pro\",\"description\":\"Updated\",\"price\":1199.99,\"stock\":5}"

# Delete product with id 1
# -X DELETE sets the HTTP method to DELETE
curl -X DELETE http://localhost:8080/api/products/1

# Check app health
# Actuator health endpoint — shows status of app, database and redis
curl http://localhost:8080/actuator/health
```

### 🛑 Step 4 — Stop PostgreSQL when done

```powershell
# Stop the local postgres container
docker stop local-postgres

# Remove it — data is lost but that is fine for local dev
docker rm local-postgres
```

---

## 🐳 Running in Docker

After the Dockerfile and docker-compose.yml are added:

```powershell
# Navigate to the spring-docker-app folder
cd docker\spring-docker\01-spring-setup\spring-docker-app

# Build and start everything
# Docker Compose builds the app image and starts postgres
# The docker profile activates automatically via SPRING_PROFILES_ACTIVE
docker compose up -d

# Follow the logs to see the app starting
# Wait for "Started SpringDockerAppApplication"
docker compose logs -f app

# Test the API — same commands as local, same port
curl http://localhost:8080/api/products

# Stop everything
docker compose down
```

## ❤️ Actuator Health Endpoints

---
Spring Boot Actuator exposes health endpoints that Kubernetes uses
to know if your app is alive and ready to receive traffic.

```powershell
# Overall health — shows all components
curl http://localhost:8080/actuator/health

# Liveness probe — is the app alive?
# Kubernetes restarts the pod if this returns DOWN
curl http://localhost:8080/actuator/health/liveness

# Readiness probe — is the app ready for traffic?
# Kubernetes removes the pod from the load balancer if this returns DOWN
curl http://localhost:8080/actuator/health/readiness
```