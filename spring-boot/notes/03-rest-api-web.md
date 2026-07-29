# 03 · REST & Web Layer

> How an HTTP request travels through a controller and back out as JSON.

---

## 🧩 The controller in one picture

```java
@RestController                          // JSON responses, not views
@RequestMapping("/api/products")         // base path for every method
@RequiredArgsConstructor                 // constructor injection (Lombok)
@Tag(name = "Products")                  // Swagger grouping
public class ProductController {

    private final ProductService service; // injected

    @GetMapping                          // GET /api/products
    public PagedResponse<ProductResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")                 // GET /api/products/42
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping                         // POST /api/products
    @ResponseStatus(HttpStatus.CREATED)  // returns 201
    public ProductResponse create(@RequestBody @Valid ProductRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")                 // PUT /api/products/42
    public ProductResponse update(@PathVariable Long id,
                                  @RequestBody @Valid ProductRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")              // DELETE /api/products/42
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

---

## 🔌 Where does the data come from? (parameter binding)

| Source | Annotation | Example URL / body |
|--------|-----------|--------------------|
| URL path segment | `@PathVariable` | `/products/**42**` |
| Query string | `@RequestParam` | `/products?page=**0**&size=**20**` |
| JSON body | `@RequestBody` | `{ "name": "Pen", "price": 9.99 }` |
| HTTP header | `@RequestHeader` | `Authorization: Bearer ...` |

---

## 📤 Returning responses: two styles

### 1. Return the object directly
```java
@GetMapping("/{id}")
public ProductResponse get(@PathVariable Long id) {
    return service.get(id);   // 200 OK + JSON body automatically
}
```
Spring + Jackson serialize it to JSON with `200 OK`.

### 2. Return `ResponseEntity` (full control)
```java
@PostMapping
public ResponseEntity<ProductResponse> create(@RequestBody @Valid ProductRequest req) {
    ProductResponse created = service.create(req);
    return ResponseEntity
            .status(HttpStatus.CREATED)          // 201
            .header("Location", "/api/products/" + created.id())
            .body(created);
}
```
Use `ResponseEntity` when you need custom status, headers, or an empty body.

---

## 🔢 HTTP status codes you must know

| Code | Meaning | When |
|------|---------|------|
| **200 OK** | success | GET/PUT that returns data |
| **201 Created** | resource created | POST that creates something |
| **204 No Content** | success, empty body | DELETE |
| **400 Bad Request** | invalid input | validation failure |
| **401 Unauthorized** | not authenticated | missing/invalid token |
| **403 Forbidden** | authenticated but not allowed | wrong role |
| **404 Not Found** | resource doesn't exist | bad id |
| **409 Conflict** | state clash | duplicate unique field |
| **500 Internal Server Error** | unhandled bug | uncaught exception |

Set them with `@ResponseStatus`, `ResponseEntity.status(...)`, or in the exception handler.

---

## 🧱 DTOs — never expose entities

- **Request DTO** (`ProductRequest`) = the shape you accept. Carries validation.
- **Response DTO** (`ProductResponse`) = the shape you return. Can hide/rename/compute fields.
- **Entity** (`Product`) = the DB mapping. Stays internal.

**Why?**
- Security — don't leak internal fields (password hashes, audit columns).
- Stability — DB changes don't break the API contract.
- Flexibility — computed/derived response fields.

```java
// Java records make great DTOs — immutable, concise
public record ProductResponse(Long id, String name, BigDecimal price, boolean inStock) {}
```

---

## 📄 Pagination

```java
@GetMapping
public PagedResponse<ProductResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    Page<Product> result = repository.findAll(PageRequest.of(page, size));
    // map to a PagedResponse wrapper (content + page metadata)
}
```
Return a **wrapper** (`PagedResponse`) exposing `content`, `page`, `size`, `totalElements`,
`totalPages` — don't leak Spring's `Page` type directly.

---

## 📖 Swagger / OpenAPI documentation

`@Tag`, `@Operation`, `@Parameter` decorate endpoints so springdoc generates interactive
docs at **`/swagger-ui.html`**. Purely documentation — no runtime behaviour change.

```java
@Operation(summary = "Get a product by id", description = "Returns 404 if not found")
@GetMapping("/{id}")
public ProductResponse get(
    @Parameter(description = "Product id", example = "42") @PathVariable Long id) { ... }
```

➡️ Next: **[04 – Data & JPA](./04-data-jpa.md)**

