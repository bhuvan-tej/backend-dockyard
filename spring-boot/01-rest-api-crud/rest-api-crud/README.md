# 🌱 REST API CRUD

## 🎯 Goal

---
Build a REST API demonstrating the patterns
every senior Java developer is expected to know — proper DTOs, Bean Validation, 
global exception handling and pagination.

## 🏗️ Architecture

---
```
HTTP Request
      │
      ▼
ProductController       receives request, validates with @Valid
      │                 returns correct HTTP status codes
      ▼
ProductService          business logic, maps DTOs to entities
      │                 throws domain exceptions
      ▼
ProductRepository       database queries via Spring Data JPA
      │                 returns Page<Product> for pagination
      ▼
PostgreSQL              products table
```

## 📁 Project Structure

---
```
rest-api-crud/
├── src/main/java/com/dockyard/restapi/
│   ├── config/
│   │   └── OpenApiConfig.java          Swagger UI configuration
│   ├── controller/
│   │   └── ProductController.java      HTTP endpoints
│   ├── dto/
│   │   ├── ProductRequest.java         input shape with validation
│   │   ├── ProductResponse.java        output shape with computed fields
│   │   └── PagedResponse.java          pagination wrapper
│   ├── entity/
│   │   └── Product.java                database table mapping
│   ├── exception/
│   │   ├── ErrorResponse.java          consistent error shape
│   │   ├── ResourceNotFoundException   thrown on 404
│   │   └── GlobalExceptionHandler      catches all exceptions
│   ├── repository/
│   │   └── ProductRepository.java      database queries
│   └── service/
│       └── ProductService.java         business logic
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

## 🔑 Key Concepts

---
### DTOs vs Entities
```
Entity        maps to database — Hibernate creates the table from this
Request DTO   what client sends — @Valid checks these before method runs
              Client cannot send id, createdAt or updatedAt
Response DTO  what we send back — we control exactly what client sees
              inStock is computed from stock — not a database column
```

### Bean Validation
```
@NotBlank   field must not be null or empty
@NotNull    field must not be null
@Size       string length between min and max
@Positive   number must be greater than zero
@Min(0)     number must be zero or greater

@Valid on @RequestBody triggers all validation
Failure → GlobalExceptionHandler returns 400 with field errors map
```

### Global Exception Handler
```
ResourceNotFoundException           → 404 Not Found
MethodArgumentNotValidException     → 400 with field errors
DataIntegrityViolationException     → 409 Conflict (duplicate name)
Exception (catch-all)               → 500 Internal Server Error

All errors return same ErrorResponse shape
```

### Pagination
```
GET /api/products?page=0&size=10&sortBy=price&sortDir=desc

Spring Data JPA generates:
  SELECT * FROM products ORDER BY price DESC LIMIT 10 OFFSET 0

PagedResponse wraps the list with metadat>
  totalElements, totalPages, last, page, size
```

---

## ✅ Running Locally

### Step 1 — Start PostgreSQL

```powershell
docker run -d `
  --name rest-api-pg `
  -e POSTGRES_USER=appuser `
  -e POSTGRES_PASSWORD=apppass `
  -e POSTGRES_DB=restdb `
  -p 5432:5432 `
  postgres:15-alpine
```

### Step 2 — Run the App

```
Open RestApiCrudApplication.java in IntelliJ
Click the green play button
Wait for: Started RestApiCrudApplication in X seconds
```

### Step 3 — Open Swagger UI

```
http://localhost:8080/api/swagger-ui.html
```

## 🧪 Testing

### Create a product
```powershell
curl -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Laptop Pro\",\"description\":\"Gaming laptop\",\"price\":1299.99,\"stock\":10,\"category\":\"Electronics\"}"
```

### Test validation — send empty body
```powershell
curl -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d "{}"
# Returns 400 with all field errors in a map
```

### Test pagination
```powershell
curl "http://localhost:8080/api/products?page=0&size=5&sortBy=price&sortDir=desc"
```

### Test search
```powershell
curl "http://localhost:8080/api/products/search?keyword=laptop"
```

### Test 404
```powershell
curl http://localhost:8080/api/products/999
# Returns: {"status":404,"error":"Not Found","message":"Product not found with id: 999"}
```

## 📋 Endpoints

---
| Method | URL                           | Description            | Status    |
|--------|-------------------------------|------------------------|-----------|
| GET    | /api/products                 | All products paginated | 200       |
| GET    | /api/products/{id}            | Single product         | 200 / 404 |
| POST   | /api/products                 | Create product         | 201 / 400 |
| PUT    | /api/products/{id}            | Update product         | 200 / 404 |
| DELETE | /api/products/{id}            | Delete product         | 204 / 404 |
| GET    | /api/products/category/{cat}  | By category            | 200       |
| GET    | /api/products/search?keyword= | Search                 | 200       |
| GET    | /api/products/in-stock        | In stock only          | 200       |
| GET    | /actuator/health              | Health check           | 200       |

## 💡 Interview Questions

---
**Q: Why use DTOs instead of exposing entities directly?**
> Entities are tied to the database schema. Exposing them directly
means database changes break the API and clients can send fields
they should not control like id or createdAt. DTOs decouple the
API contract from the database structure.

**Q: How does Bean Validation work in Spring Boot?**
> Add validation annotations to DTO fields. Add @Valid to the
@RequestBody parameter in the controller. Spring validates before
the method runs. Failure throws MethodArgumentNotValidException
which GlobalExceptionHandler catches and returns a 400 with all
field errors in a map.

**Q: What is a GlobalExceptionHandler?**
> A class with @RestControllerAdvice that intercepts exceptions from
all controllers. Without it each controller needs try-catch blocks
and errors have inconsistent formats. With it all errors are handled
in one place with the same response shape.

**Q: How does pagination work in Spring Data JPA?**
> Pass a Pageable object to repository methods. Spring generates
SQL LIMIT and OFFSET automatically. The returned Page object
contains the content list plus metadata like totalElements and
totalPages.