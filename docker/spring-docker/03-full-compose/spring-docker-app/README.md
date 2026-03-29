# 🐳 Production Stack

## 🎯 Goal

---
Add Nginx as a reverse proxy in front of the Spring Boot app.
This is the closest to a real production Docker setup you will see before moving to Kubernetes

## 🏗️ Architecture

---
```
Your Browser
     │
     │  http://localhost:80
     ▼
┌─────────────┐
│    Nginx    │  port 80 — the only port exposed to outside
│             │  reverse proxy, single entry point
└──────┬──────┘
       │
       │  http://app:8080  (internal Docker DNS)
       ▼
┌─────────────┐
│ Spring Boot │  port 8080 — NOT exposed to outside
│     app     │  only reachable via Nginx
└──────┬──────┘
       │
       ├──────────────────────────┐
       ▼                          ▼
┌─────────────┐          ┌─────────────┐
│  PostgreSQL │          │    Redis    │
│  port 5432  │          │  port 6379  │
└─────────────┘          └─────────────┘
```

## 📁 Project Structure

---
```
spring-docker-app/
├── src/
│   ├── main/
│   │   ├── java/com/dockyard/springdockerapp/
│   │   │   ├── SpringDockerAppApplication.java   entry point
│   │   │   ├── config/
│   │   │   │   └── RedisConfig.java              redis cache configuration
│   │   │   ├── entity/
│   │   │   │   └── Product.java                  database table mapping
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java        database queries
│   │   │   ├── service/
│   │   │   │   └── ProductService.java           business logic with caching
│   │   │   └── controller/
│   │   │       └── ProductController.java        HTTP endpoints
│   │   └── resources/
│   │       ├── application.yml                   base config for local run
│   │       └── application-docker.yml            overrides for Docker profile
│   └── test/
├── nginx/
│   └── nginx.conf                                reverse proxy configuration
├── Dockerfile                                    multi-stage build
├── docker-compose.yml                            full stack with Nginx
├── .dockerignore                                 excludes target .idea .git
├── .gitignore
└── pom.xml
```

## 🔄 What Changed From 02-redis-cache

---
### 1 — version line removed
```yaml
# REMOVED
version: "3.8"
```
**Why:** Docker Compose no longer needs this line. Newer versions show a warning if it is present.
 
---

### 2 — Container names changed
```
# 02-redis-cache                  →   # 03-full-compose
container_name: spring-postgres   →   container_name: full-postgres
container_name: spring-redis      →   container_name: full-redis
container_name: spring-app        →   container_name: full-app
```
**Why:** Each folder runs as a separate stack. Unique names prevent conflicts if both stacks run at the same time.
 
---

### 3 — App ports removed
```yaml
# 02-redis-cache — app directly accessible on 8080
ports:
  - "8080:8080"
 
# 03-full-compose — removed entirely from app service
# no ports section on app
```
**Why:** App is now internal only. All traffic must go through Nginx on port 80.
 
---

### 4 — App healthcheck added
```yaml
# ADDED in 03 — did not exist in 02
healthcheck:
  test: ["CMD-SHELL", "wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 40s
```
**Why:** Nginx needs to know when the app is ready before routing traffic. Without this Nginx starts immediately and returns 502 Bad Gateway errors during app startup.
 
---

### 5 — Nginx service added
```yaml
# ADDED in 03 — did not exist in 02
nginx:
  image: nginx:1.25-alpine
  container_name: full-nginx
  ports:
    - "80:80"
  volumes:
    - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
  depends_on:
    app:
      condition: service_healthy
  networks:
    - app-network
```
**Why:** Nginx becomes the single entry point on port 80 and waits for the app to be healthy before accepting traffic.

### 📊 Summary

---
```
02-redis-cache               03-full-compose
──────────────               ───────────────
version: "3.8"          →    removed
ports 8080 on app       →    removed (app is internal)
no app healthcheck      →    added (Nginx waits for it)
no Nginx service        →    added (reverse proxy)
port 80 not exposed     →    port 80 exposed via Nginx
```

## 🌐 Why Nginx in Front of Spring Boot?

---
### 🔀 Without Nginx
```
Browser → Spring Boot :8080 directly
 
Problems:
  App server handles every raw connection itself
  Port 8080 is exposed directly to the internet
  Adding SSL means changing Spring Boot config
  Scaling means clients need to know multiple ports
  No protection against slow clients or traffic spikes
```

### ✅ With Nginx
```
Browser → Nginx :80 → Spring Boot :8080 (internal)
 
Benefits:
  Spring Boot is never exposed directly to the internet
  Single entry point on port 80 (standard HTTP)
  SSL added at Nginx level without touching app code
  Slow clients handled by Nginx — Spring Boot threads freed quickly
  Easy to scale — add more app instances to the upstream block
  Rate limiting and request filtering at the proxy level
  Static files served by Nginx — faster than Spring Boot
```

### 🔄 Request Flow

---
```
1. Browser sends request to localhost:80
2. Nginx receives it and checks the location block
3. Nginx forwards to http://app:8080 via Docker DNS
4. Spring Boot processes and returns response to Nginx
5. Nginx returns response to browser
 
The browser never knows Spring Boot exists.
The browser only ever talks to Nginx.
```

### 🏭 How Real Production Uses This

---
```
Internet → Load Balancer → Nginx → Spring Boot instance 1
                                 → Spring Boot instance 2
                                 → Spring Boot instance 3
 
In Kubernetes this role is taken by an Ingress controller
which works exactly like Nginx but at the cluster level.
You will build this in the Kubernetes section.
```

## 💡 Interview Questions

---
**Q: What is a reverse proxy and why use Nginx in front of Spring Boot?**
> A reverse proxy sits between clients and backend servers forwarding
requests on their behalf. Nginx handles SSL termination, load
balancing and rate limiting at the edge so Spring Boot only
handles application logic.

**Q: What is the difference between a forward proxy and a reverse proxy?**
> A forward proxy sits in front of clients and hides them from servers
for example a VPN. A reverse proxy sits in front of servers and
hides them from clients for example Nginx in front of Spring Boot.

**Q: Why is Spring Boot not exposed on port 8080 in production?**
> Backend servers should not be directly reachable from the internet.
All traffic goes through the reverse proxy which applies security
rules and SSL. Scaling is also easier — add more instances behind
Nginx without clients knowing.

**Q: What does depends_on condition service_healthy do for Nginx?**
> It prevents Nginx from starting until the app passes its healthcheck.
Without this Nginx routes traffic before the app is ready and
clients get 502 Bad Gateway errors during startup.

**Q: How does Nginx find the Spring Boot app inside Docker?**
> Docker DNS resolves the service name app to the Spring Boot
container IP automatically. Configured in nginx.conf as
proxy_pass http://app:8080.