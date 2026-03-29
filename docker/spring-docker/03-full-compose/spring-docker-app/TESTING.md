# 🧪 Testing Guide — Full Production Stack

---

## 🌐 API Testing Through Nginx

All requests go through Nginx on port 80.
The app is not accessible directly on port 8080.

### Create a Product
```powershell
# -X POST sets the HTTP method to POST
# -H sets the Content-Type header so the server knows we are sending JSON
# -d sets the request body as a JSON string
curl -X POST http://localhost/api/products `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Laptop\",\"description\":\"Gaming laptop\",\"price\":999.99,\"stock\":10}"

# Expected response — product created with auto-generated id
# {"id":1,"name":"Laptop","description":"Gaming laptop","price":999.99,"stock":10}
```

### Get All Products
```powershell
# GET is the default method so no -X flag needed
curl http://localhost/api/products

# Expected response — array of all products
# [{"id":1,"name":"Laptop",...}]
```

### Get Product by ID
```powershell
# The id is part of the URL path
curl http://localhost/api/products/1

# Expected response — single product object
# {"id":1,"name":"Laptop",...}
```

### Update a Product
```powershell
# -X PUT sets the HTTP method to PUT
curl -X PUT http://localhost/api/products/1 `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Laptop Pro\",\"description\":\"Updated\",\"price\":1199.99,\"stock\":5}"

# Expected response — updated product
```

### Delete a Product
```powershell
# -X DELETE sets the HTTP method to DELETE
curl -X DELETE http://localhost/api/products/1

# Expected response — 204 No Content (empty body, success)
```

---

## 🔒 Proving the App Is Not Directly Accessible

```powershell
# Try to reach Spring Boot directly on port 8080
# This should FAIL — port 8080 is not mapped to your Windows machine
curl http://localhost:8080/api/products
# Expected: Connection refused

# The only way in is through Nginx on port 80
curl http://localhost/api/products
# Expected: products array returned successfully
```

---

## ❤️ Health Checks

```powershell
# Overall app health — shows all components
curl http://localhost/actuator/health

# Expected response:
# {
#   "status": "UP",
#   "components": {
#     "db":    { "status": "UP" },
#     "redis": { "status": "UP" },
#     "ping":  { "status": "UP" }
#   }
# }

# Liveness probe — is the app alive
# Kubernetes restarts the pod if this returns DOWN
curl http://localhost/actuator/health/liveness

# Readiness probe — is the app ready for traffic
# Kubernetes removes the pod from load balancer if this returns DOWN
curl http://localhost/actuator/health/readiness
```

---

## ⚡ Proving Redis Cache Works

```powershell
# Create a product first
curl -X POST http://localhost/api/products `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Phone\",\"description\":\"Smartphone\",\"price\":499.99,\"stock\":20}"

# First GET by id — hits the database
curl http://localhost/api/products/1

# Check app logs — you will see the Hibernate SQL query
docker compose logs app --tail 10
# Look for: Hibernate: select p1_0.id...

# Second GET by id — served from Redis cache
curl http://localhost/api/products/1

# Check app logs again — NO Hibernate SQL this time
docker compose logs app --tail 10
# No Hibernate line — data came from Redis
```

### Inspect Redis Directly
```powershell
# Open Redis CLI inside the container
docker compose exec redis redis-cli

# List all cache keys
KEYS *
# Expected: "products::1"

# Read the cached product as JSON
GET "products::1"

# Exit Redis CLI
exit
```

---

## 📊 Watching Nginx Logs

```powershell
# Follow Nginx access logs in real time
# Shows every request Nginx receives and proxies
docker compose logs -f nginx

# In another terminal make some requests
curl http://localhost/api/products
curl http://localhost/api/products/1
curl http://localhost/actuator/health

# In the nginx log you will see lines like:
# 172.18.0.1 - - [date] "GET /api/products HTTP/1.1" 200 ...
# 172.18.0.1 - - [date] "GET /api/products/1 HTTP/1.1" 200 ...
```

---

## 🔍 Inspecting Services From Inside

### Inspect Nginx
```powershell
# Shell into the Nginx container
docker compose exec nginx sh

# Verify our config is mounted correctly
cat /etc/nginx/nginx.conf

# Check access logs for all requests Nginx has handled
cat /var/log/nginx/access.log

# Check error logs — should be empty if everything is working
cat /var/log/nginx/error.log

# Exit
exit
```

### Inspect the Database
```powershell
# Connect to PostgreSQL using psql inside the container
# -U appuser connects as our database user
# -d appdb connects to our database
docker compose exec postgres psql -U appuser -d appdb

# List all tables
\dt

# See all products in the database
SELECT * FROM products;

# Exit psql
\q
```

### Inspect the App Container
```powershell
# Shell into the Spring Boot container
docker compose exec app sh

# Check environment variables — confirm docker profile is active
env | grep SPRING

# Check the JAR is there
ls /app

# Check JVM flags are set
echo $JAVA_OPTS

# Exit
exit
```

---

## 🌡️ Resource Usage

```powershell
# See CPU and memory usage for all four containers
# Useful to check nothing is using too much memory
docker stats --no-stream

# Live stream of resource usage
# Press Ctrl+C to stop
docker stats
```