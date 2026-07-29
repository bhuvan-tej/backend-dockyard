# 06 · Exception Handling

> One place to turn any error into a clean, consistent JSON response.

---

## 🎯 The goal

Without central handling, an uncaught exception leaks a stack trace and returns a messy
`500`. We want:

- **Consistent shape** for every error.
- **Correct status code** (404, 400, 409, 401…).
- **No stack traces** exposed to clients.

---

## 🧯 The global handler

```java
@RestControllerAdvice                        // applies to ALL controllers, returns JSON
@Slf4j
public class GlobalExceptionHandler {

    // 404 — domain "not found"
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage(), null);
    }

    // 400 — bean validation failure
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return new ErrorResponse(400, "Validation failed", errors);
    }

    // 500 — catch-all safety net
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);          // log full detail server-side
        return new ErrorResponse(500, "Something went wrong", null); // hide details from client
    }
}
```

### The two annotations

| Annotation | Role |
|-----------|------|
| `@RestControllerAdvice` | `@ControllerAdvice` + `@ResponseBody`. A **global** interceptor for exceptions across every controller, returning JSON. |
| `@ExceptionHandler(X.class)` | "This method handles exception `X`." Most specific match wins. |

`@ResponseStatus` on the handler method sets the HTTP status of the response.

---

## 🧱 The consistent error shape

```java
@JsonInclude(JsonInclude.Include.NON_NULL)   // hide null fields (e.g. errors map)
public record ErrorResponse(
        int status,
        String message,
        Map<String, String> errors          // only present for validation errors
) {}
```

`@JsonInclude(NON_NULL)` keeps the payload tidy — the `errors` map only appears when relevant.

---

## 🏷️ Custom domain exceptions

```java
@ResponseStatus(HttpStatus.NOT_FOUND)        // optional: default status if unhandled
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

Thrown from the service, caught by the advice:

```java
public ProductResponse get(Long id) {
    Product p = repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    return toResponse(p);
}
```

> **Extend `RuntimeException` (unchecked)** for domain errors so `@Transactional` rolls back
> and you don't pollute signatures with `throws`.

---

## 🗺️ Mapping errors to status codes

| Exception | Status | Meaning |
|-----------|--------|---------|
| `ResourceNotFoundException` | 404 | id doesn't exist |
| `MethodArgumentNotValidException` | 400 | `@Valid` failed |
| `IllegalArgumentException` | 400 | bad input |
| `DataIntegrityViolationException` | 409 | unique/constraint clash |
| `AiUnavailableException` (project 04/05) | 503 | AI model down |
| `Exception` (catch-all) | 500 | unexpected bug |

---

## ⚖️ `@RestControllerAdvice` vs local `@ExceptionHandler`

- Put the handler **inside a controller** → applies only to that controller.
- Put it in a `@RestControllerAdvice` class → applies **globally**. ✅ Preferred.

---

## 🔐 Golden rules

1. **Log server-side, hide client-side** — never return stack traces to users.
2. **One error shape** across the whole API.
3. **Right status code** — clients rely on it.
4. Have a **catch-all** `Exception` handler so nothing leaks raw.

➡️ Next: **[07 – DI & Beans](./07-dependency-injection-beans.md)**

