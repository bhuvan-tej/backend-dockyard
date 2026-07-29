# 05 · Bean Validation

> Reject bad input at the boundary, automatically, with declarative annotations.

---

## 🔗 The two halves that work together

Validation needs **both** pieces:

1. **Constraint annotations** on the DTO fields (`@NotBlank`, `@Size`, …) — *declare* the rules.
2. **`@Valid`** on the controller parameter — *triggers* the check.

```java
// 1. Declare rules on the DTO
public record ProductRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be 2–100 chars")
        String name,

        @NotNull @Positive(message = "Price must be > 0")
        BigDecimal price,

        @Min(value = 0, message = "Stock cannot be negative")
        int stock
) {}

// 2. Trigger the rules in the controller
@PostMapping
public ProductResponse create(@RequestBody @Valid ProductRequest req) { ... }
```

⚠️ **Remove `@Valid` and nothing is validated** — the annotations become silent.

---

## 📋 Constraint cheat sheet

| Annotation | Applies to | Passes when |
|-----------|-----------|-------------|
| `@NotNull` | any object | value is not `null` |
| `@NotEmpty` | String/Collection/Map/array | not null **and** size > 0 |
| `@NotBlank` | String | not null **and** has non-whitespace text |
| `@Size(min, max)` | String/Collection… | length/size in range |
| `@Min` / `@Max` | numbers | within numeric bound |
| `@Positive` / `@PositiveOrZero` | numbers | > 0 / ≥ 0 |
| `@Negative` / `@NegativeOrZero` | numbers | < 0 / ≤ 0 |
| `@Pattern(regexp)` | String | matches regex |
| `@Email` | String | valid email format |
| `@Past` / `@Future` | dates | before / after now |

### Null vs blank vs empty (the classic confusion)

| Value | `@NotNull` | `@NotEmpty` | `@NotBlank` |
|-------|:---------:|:-----------:|:-----------:|
| `null` | ❌ | ❌ | ❌ |
| `""` | ✅ | ❌ | ❌ |
| `"   "` | ✅ | ✅ | ❌ |
| `"hi"` | ✅ | ✅ | ✅ |

> Rule of thumb: **Strings → `@NotBlank`**, **collections → `@NotEmpty`**, **objects/numbers → `@NotNull`**.

---

## 💬 Custom messages

Every constraint accepts `message`:

```java
@Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits")
String code;
```
(From `03-otp-jwt-auth` — validating a 6-digit OTP.)

---

## 🧯 What happens when validation fails?

Spring throws `MethodArgumentNotValidException` → returns **400 Bad Request**.
To make errors clean and consistent, handle it in the global advice:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
      .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
    return new ErrorResponse(400, "Validation failed", fieldErrors);
}
```

Client gets:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": { "name": "Name is required", "price": "Price must be > 0" }
}
```

---

## 🪆 Nested & collection validation

Put `@Valid` on nested objects/lists so their constraints run too:

```java
public record OrderRequest(
    @NotNull @Valid CustomerDto customer,          // validate inside
    @NotEmpty @Valid List<@Valid ItemDto> items    // validate each element
) {}
```

---

## 🧩 Validating other inputs

- `@RequestParam`/`@PathVariable`: add `@Validated` on the **class** and put constraints
  directly on the params (`@Min(1) @PathVariable Long id`).
- `@ConfigurationProperties`: constraints work if the class is `@Validated`.

➡️ Next: **[06 – Exception Handling](./06-exception-handling.md)**

