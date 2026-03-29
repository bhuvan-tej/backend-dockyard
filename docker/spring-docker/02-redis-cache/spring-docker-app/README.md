# Redis Caching

## 🎯 Goal

---
Add Redis caching to the Spring Boot Products API.
Understand what caching is, why it matters, and prove it is working
by watching database queries disappear after the first request.

## What Is Caching and Why It Matters

---
```
WITHOUT CACHE
 
  Client → GET /api/products/1
  App    → SELECT * FROM products WHERE id = 1  (hits database)
  App    → returns product
 
  Client → GET /api/products/1  (same request again)
  App    → SELECT * FROM products WHERE id = 1  (hits database AGAIN)
  App    → returns product
 
  Every request hits the database even when the data has not changed.
  For read-heavy endpoints this is wasteful and slow.
 
WITH CACHE
 
  Client → GET /api/products/1
  App    → cache miss — product not in Redis yet
  App    → SELECT * FROM products WHERE id = 1  (hits database)
  App    → saves result in Redis with key "products::1"
  App    → returns product
 
  Client → GET /api/products/1  (same request again)
  App    → cache hit — product found in Redis
  App    → returns product directly from Redis
  Database is never touched on the second request
 
  Redis is much faster than PostgreSQL for simple key lookups.
  Typical database query: 5–50ms
  Typical Redis lookup:   0.1–1ms
```

## What Changed From 01-spring-setup

---
```
Added:
  config/RedisConfig.java        configures Redis as the cache provider
                                 sets TTL, key and value serialisers
 
Updated:
  service/ProductService.java    added @Cacheable on getProductById
                                 added @CachePut on updateProduct
                                 added @CacheEvict on deleteProduct
 
Updated:
  application.yml                added spring.cache.type=redis
```

Everything else is identical to `01-spring-setup`.

## The Three Caching Annotations

---
```
@Cacheable    Check Redis first. If found return cached value.
              If not found run the method, save result to Redis, return it.
              Used on READ operations.
 
@CachePut     Always run the method AND update Redis with the new result.
              Used on UPDATE operations to keep cache in sync.
 
@CacheEvict   Remove the entry from Redis.
              Used on DELETE operations so stale data is not returned.
```

## Project Structure

---
```
spring-docker-app/
├── src/
│   ├── main/
│   │   ├── java/com/dockyard/springdockerapp/
│   │   │   ├── SpringDockerAppApplication.java   entry point
│   │   │   ├── config/
│   │   │   │   └── RedisConfig.java              redis configuration
│   │   │   ├── entity/
│   │   │   │   └── Product.java                  database table mapping
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java        database queries
│   │   │   ├── service/
│   │   │   │   └── ProductService.java           business logic
│   │   │   └── controller/
│   │   │       └── ProductController.java        HTTP endpoints
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

## Running Locally in IntelliJ

---
### Step 1 — Start the Stack

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

## Interview Questions

---
**Q: What is caching and why is it used?**
> Caching stores the result of an expensive operation like a database
query in fast storage like Redis. Subsequent requests for the same
data are served from the cache instead of hitting the database.
This reduces database load and response time significantly.

**Q: What is the difference between @Cacheable and @CachePut?**
> @Cacheable skips the method entirely if the result is already in
the cache. @CachePut always runs the method but also updates the
cache with the new result. @Cacheable is for reads, @CachePut is
for updates to keep the cache in sync.

**Q: What is cache eviction?**
> Removing an entry from the cache. @CacheEvict does this in Spring.
Used on delete operations so the cache does not return data for
something that no longer exists in the database.

**Q: What is TTL in caching?**
> Time To Live — how long a cached entry stays before it expires
automatically. After expiry the next request goes to the database
and the result is cached again with a fresh TTL.
Set to 10 minutes in this project via RedisConfig.

**Q: What happens if Redis goes down?**
> Spring Boot falls back to hitting the database directly.
The app continues to work, just without the caching benefit.
This is why caching is a performance optimisation, not a
critical dependency.