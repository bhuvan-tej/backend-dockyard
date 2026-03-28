# Spring Boot Setup and Containerisation

## 🎯 Goal

---
Take a real Spring Boot REST API with PostgreSQL and containerise it
properly using a multi-stage Dockerfile and Spring profiles.
Understand how the app switches between local and Docker configuration
without changing any code.

## What This Project Does

---
A simple Products REST API backed by PostgreSQL.
Every product has a name, description, price and stock quantity.
The API supports full CRUD — create, read, update and delete.

## Project Structure

---
```
spring-docker-app/
├── src/
│   ├── main/
│   │   ├── java/com/dockyard/springdockerapp/
│   │   │   ├── SpringDockerAppApplication.java   entry point
│   │   │   ├── entity/
│   │   │   │   └── Product.java                  database table mapping
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java         database queries
│   │   │   ├── service/
│   │   │   │   └── ProductService.java            business logic
│   │   │   └── controller/
│   │   │       └── ProductController.java         HTTP endpoints
│   │   └── resources/
│   │       ├── application.yml                   base config (local)
│   │       └── application-docker.yml            docker profile config
│   └── test/
├── Dockerfile                                    multi-stage build
├── docker-compose.yml                            full stack setup
├── .dockerignore                                 exclude junk from build
├── .gitignore
├── README.md                                     you are here
└── pom.xml
```

## How Spring Profiles Work

---
```
application.yml                 base configuration, always loaded
application-docker.yml          loaded on top when profile = docker

Local run in IntelliJ:
  Uses application.yml
  Connects to localhost:5432 (your local PostgreSQL)

Docker run:
  Uses application.yml + application-docker.yml
  application-docker.yml overrides the database URL
  to use "postgres" hostname instead of "localhost"
  Docker DNS resolves "postgres" to the postgres container IP

You never change code between environments.
Only configuration changes.
```

## API Endpoints

---
| Method | URL                               | What it does               |
|--------|-----------------------------------|----------------------------|
| GET    | /api/products                     | Get all products           |
| GET    | /api/products/{id}                | Get one product by ID      |
| POST   | /api/products                     | Create a new product       |
| PUT    | /api/products/{id}                | Update a product           |
| DELETE | /api/products/{id}                | Delete a product           |
| GET    | /api/products/search?maxPrice=100 | Products under a price     |
| GET    | /api/products/in-stock            | Products with stock > 0    |
| GET    | /actuator/health                  | App health check           |
| GET    | /actuator/health/liveness         | Kubernetes liveness probe  |
| GET    | /actuator/health/readiness        | Kubernetes readiness probe |

## Running Locally in IntelliJ

---
### Step 1 — Start PostgreSQL in Docker

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

### Step 2 — Run the App

Open `SpringDockerAppApplication.java` in IntelliJ and click the
green play button next to the main method.

Wait for this line in the console:
```
Started SpringDockerAppApplication in X seconds
```

### Step 3 — Test the API

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

### Step 4 — Stop PostgreSQL when done

```powershell
# Stop the local postgres container
docker stop local-postgres

# Remove it — data is lost but that is fine for local dev
docker rm local-postgres
```

---

## Running in Docker

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

## Actuator Health Endpoints

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

## Interview Questions

---
**Q: What is a Spring profile and why is it useful in Docker?**
> A profile is a named set of configuration that activates in specific
environments. application-docker.yml activates when the docker profile
is set. This lets the app connect to postgres by container name in
Docker and localhost when running locally without changing any code.

**Q: What is Spring Data JPA and what does it do?**
> It is a Spring abstraction over JPA and Hibernate. It generates
database queries automatically from repository method names.
findByName() generates SELECT FROM products WHERE name equals
without writing any SQL.

**Q: What is Spring Boot Actuator?**
> A library that adds production ready endpoints to your app.
The health endpoint shows if the app and its dependencies are up.
Kubernetes uses the liveness and readiness endpoints to manage
pod lifecycle automatically.

**Q: What is the difference between liveness and readiness probes?**
> Liveness checks if the app is alive — failure causes a restart.
Readiness checks if the app is ready for traffic — failure removes
it from the load balancer without restarting it. An app can be
alive but not ready, for example during startup or when the
database connection pool is exhausted.