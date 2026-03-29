## 🖥️ Running Locally in IntelliJ

---
### 🔌 Step 1 — Start the Stack

```powershell
# Navigate to the 02-redis-cache app folder
cd docker\spring-docker\02-redis-cache\spring-docker-app
 
# Build and start everything
# --build forces a fresh image build
docker compose up -d --build
 
# Watch the logs and wait for Started SpringDockerAppApplication
docker compose logs -f app
```

### Step 2 — Create a Product

```powershell
# Create a product to cache
# -X POST sets the HTTP method
# -H sets the Content-Type header so the app knows we are sending JSON
# -d sets the request body
curl -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Laptop\",\"description\":\"Gaming laptop\",\"price\":999.99,\"stock\":10}"
```

### Step 3 — Prove Caching Is Working

```powershell
# First GET — watch the IntelliJ or docker logs
# You will see two lines:
#   Fetching product with id: 1 from database
#   Hibernate: select p1_0.id... (the actual SQL query)
curl http://localhost:8080/api/products/1
 
# Second GET — watch the logs again
# You will see ONE line:
#   Fetching product with id: 1 from database
# But NO Hibernate SQL query
# The data came from Redis, the database was never touched
curl http://localhost:8080/api/products/1
 
# Third GET — no SQL query again
# Redis serves all subsequent requests until the cache expires
curl http://localhost:8080/api/products/1
```

### Step 4 — Inspect the Cache in Redis Directly

```powershell
# Open the Redis CLI inside the Redis container
# docker compose exec runs a command inside a running service
# redis is the service name in docker-compose.yml
# redis-cli is the Redis command line client
docker compose exec redis redis-cli
 
# Inside Redis CLI — list all cache keys
# KEYS * shows every key stored in Redis
# You should see something like: "products::1"
KEYS *
 
# Read the cached value for product 1
# GET retrieves the value stored under this key
# You will see the product as a JSON string
GET "products::1"
 
# Exit Redis CLI
exit
```

## Step 5 — Prove Cache Eviction on Delete

```powershell
# Check the cache — product 1 is there
docker compose exec redis redis-cli KEYS *
 
# Delete product 1 — @CacheEvict removes it from Redis
# -X DELETE sets the HTTP method to DELETE
curl -X DELETE http://localhost:8080/api/products/1
 
# Check the cache again — products::1 is gone
docker compose exec redis redis-cli KEYS *
 
# GET product 1 — goes to database now, returns 404
curl http://localhost:8080/api/products/1
```

## Step 6 — Prove Cache Update on PUT

```powershell
# Create a product first
curl -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Phone\",\"description\":\"Smartphone\",\"price\":499.99,\"stock\":20}"
 
# GET it once to populate the cache
curl http://localhost:8080/api/products/1
 
# Verify it is in Redis
docker compose exec redis redis-cli GET "products::1"
 
# Update the product — @CachePut updates the cache with new values
curl -X PUT http://localhost:8080/api/products/1 `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Phone Pro\",\"description\":\"Updated\",\"price\":599.99,\"stock\":15}"
 
# GET it again — returns updated values from cache, no SQL query
curl http://localhost:8080/api/products/1
 
# Verify the cache has the new values
docker compose exec redis redis-cli GET "products::1"
```

## Step 7 — Clean Up

```powershell
# Stop everything and remove containers
docker compose down
 
# Stop and also remove volumes (wipes database data)
docker compose down -v
```